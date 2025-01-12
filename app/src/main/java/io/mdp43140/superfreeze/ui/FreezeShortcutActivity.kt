/*
 * SPDX-FileCopyrightText: 2025 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.mdp43140.superfreeze.ui
import android.app.Activity
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import android.net.Uri
import com.topjohnwu.superuser.Shell
import io.mdp43140.superfreeze.App
import io.mdp43140.superfreeze.AppListItems
import io.mdp43140.superfreeze.AppListItems.AppItem
import io.mdp43140.superfreeze.databinding.ActivityEmptyBinding
import io.mdp43140.superfreeze.FreezeService
import io.mdp43140.superfreeze.R

class FreezeShortcutActivity: Activity(){
	override fun onCreate(savedInstanceState:Bundle?){
		super.onCreate(savedInstanceState)
		ActivityEmptyBinding.inflate(layoutInflater).apply {
			setContentView(root)
		}
		freezeApp(this)
	}
	override fun onDestroy(){
		super.onDestroy()
		FreezeService.finishedFreezing()
		finish()
	}
	override fun onResume() {
		super.onResume()
		// TODO: when using manual/accessibility,
		// on back button pressed (which goes back to this activity)
		// and also uhh:
		if (appsToBeFrozenIter?.hasNext() == true) {
			freezeApp(this,listOf(appsToBeFrozenIter!!.next()))
		} else {
			appsToBeFrozenIter = null
			finish()
		}
	}
	companion object {
		var appsToBeFrozen: List<AppItem>? = null
		var appsToBeFrozenIter: ListIterator<AppItem>? = null
		var isRunning: Boolean = false
		fun freezeApp(ctx: Activity){
			// temporary workaround: initialize the var here to avoid potential crash
			// TODO: still not work, if this being accessed will immediately crash
			if (App.appListItems == null){
				App.appListItems = AppListItems(ctx)
				App.appListItems?.loadPrefs()
			}
			App.appListItems?.loadApps()
			App.appListItems?.getDataFromPrefs()
			App.appListItems?.getActiveAccessibilityServices()
			App.appListItems?.getEnabledIMEs()
			freezeApp(ctx,App.appListItems!!.getPendingStopApps());
		}
		fun freezeApp(ctx: Activity, apps: List<AppItem>){
			stopBgApps(ctx,apps)
			if (isRunning){
				Toast
					.makeText(ctx,ctx.getString(R.string.currently_stopping),Toast.LENGTH_SHORT)
					.show()
				return
			}
			if (apps.isNullOrEmpty()){
				Toast
					.makeText(ctx,ctx.getString(R.string.all_apps_stopped),Toast.LENGTH_SHORT)
					.show()
				return
			}
			if (App.workMode == "manual" || App.workMode == "accessibility"){
				if (App.workMode == "manual"){
					Toast
						.makeText(ctx,ctx.getString(R.string.manual_stop_tip),Toast.LENGTH_SHORT)
						.show()
				}
				apps.forEach {
					val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
						addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
						addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
						addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION)
					//data = Uri.fromParts("package",it.pkg,null)
						data = Uri.parse("package:${it.pkg}")
					}
					if (it.stopMode != 1){
						Toast
							.makeText(ctx,"Skipping app with inactive stop mode as we dont have enough privilege for this...",Toast.LENGTH_SHORT)
							.show();
					}
					else if (App.workMode == "manual"){
						// Manual
						ctx.startActivity(intent)
					}
					else if (App.workMode == "accessibility"){
						// Accessibility
					//FreezeService.doOnAppCouldNotBeFrozen = ::fun
						FreezeService.stopAnyCurrentFreezing()
						FreezeService.clickFreezeButtons(ctx,apps.size)
						ctx.startActivity(intent)
					}
				}
				Toast
					.makeText(ctx,ctx.getString(R.string.apps_is_stopped,apps.size),Toast.LENGTH_SHORT)
					.show()
				isRunning = false
			}
			else if (App.workMode == "root"){
				val sh: Shell.Job = Shell.getShell().newJob()
				if (!Shell.rootAccess()){
					Toast
						.makeText(ctx,ctx.getString(R.string.root_not_avail),Toast.LENGTH_SHORT)
						.show()
					return
				}
				// Fun fact: these commands only needs shell (2000) level privilege
				// but sadly methods other than root is not convenient to set up
				// you will need computers, or initialize adb connection
				// and those aren't permanent either
				val setInactiveBeforeNormalStop = App.prefs!!.getBoolean("rootStop_setInactiveBeforeNormalStop",false)
				val restrictStandby = App.prefs!!.getBoolean("rootStop_restrictStandby",false)
				val setGlobHibernation = App.prefs!!.getBoolean("rootStop_setGlobHibernation",false)
				val setRestrictionLvl = App.prefs!!.getString("rootStop_setRestrictionLvl","dontChange")
				val setStopType = App.prefs!!.getString("rootStop_setStopType","force-stop")
				val stopTypeArrKey = ctx.resources.getStringArray(R.array.setStopType_key)
				val restrictionLvlArrKey = ctx.resources.getStringArray(R.array.setRestrictionLvl_key)
				apps.forEach {
					if (it.stopMode == 2 || (it.stopMode == 1 && setInactiveBeforeNormalStop)){
						// Sets inactive mode (equivalent to Greenify's shallow hibernation)
						// Great for most messenger and some social media apps
						sh.add("am set-inactive ${it.pkg} true")
					}
					if (restrictStandby){
						// Set the app to restricted standby mode
						// TODO: may restrict important apps from running in background?
						sh.add(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
							"am set-standby-bucket ${it.pkg} restricted"
						else
							"am set-standby-bucket ${it.pkg} rare"
						)
					}
					if (setGlobHibernation){
						// sets app in hibernation mode, which:
						// - Clears cache (Android 12+)
						// - Resets permission (Android 11+, Android 6-10 with GmsCore)
						//   some people definitely don't want reconfiguring permission,
						//   so i decided to disable this by default
						sh.add("cmd app_hibernation set-state --global ${it.pkg} true")
					}
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
						// Some modes [unrestricted|exempted] wont be added as an option because it does the opposite of stopping the app)
						when (setStopType){
							restrictionLvlArrKey[1] -> sh.add("am set-bg-restriction-level --user 0 ${it.pkg} adaptive_bucket")
							restrictionLvlArrKey[2] -> sh.add("am set-bg-restriction-level --user 0 ${it.pkg} restricted_bucket")
							restrictionLvlArrKey[3] -> sh.add("am set-bg-restriction-level --user 0 ${it.pkg} background_restricted")
							restrictionLvlArrKey[4] -> sh.add("am set-bg-restriction-level --user 0 ${it.pkg} hibernation")
						}
					}
					if (it.stopMode == 1){
						// PS: putting "all" or "current" before process name wont
						//     do anything to app, dont know why that's the case
						when (setStopType){
							stopTypeArrKey[0] -> sh.add("am kill ${it.pkg}")
							stopTypeArrKey[1] -> sh.add("am stop-app ${it.pkg}")
							stopTypeArrKey[2] -> sh.add("am force-stop ${it.pkg}")
						}
					}
				}
				sh.submit { _ -> ctx.runOnUiThread {
					isRunning = false
					Toast
						.makeText(ctx,ctx.getString(R.string.apps_is_stopped,apps.size),Toast.LENGTH_SHORT)
						.show()
				}}
			}
		}
		fun stopBgApps(ctx: Context, apps: List<AppItem>){
			val am = ctx.getSystemService(ActivityManager::class.java)
			apps.forEach { am.killBackgroundProcesses(it.pkg) }
		}
	}
	fun turnOffScreen(){
//if (screenOffAfterFinished) Shell.cmd("input keyevent 26").exec()
	}
}
