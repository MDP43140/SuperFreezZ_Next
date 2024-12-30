/*
 * SPDX-FileCopyrightText: 2024 MDP43140
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
import io.mdp43140.superfreeze.App
import io.mdp43140.superfreeze.AppListItems
import io.mdp43140.superfreeze.AppListItems.AppItem
import io.mdp43140.superfreeze.databinding.ActivityEmptyBinding
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
			if (App.workMode == "manual"){
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
				}
				Toast
					.makeText(ctx,ctx.getString(R.string.apps_is_stopped,apps.size),Toast.LENGTH_SHORT)
					.show()
				isRunning = false
			}
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
