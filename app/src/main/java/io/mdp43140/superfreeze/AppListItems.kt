/*
 * SPDX-FileCopyrightText: 2024 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.mdp43140.superfreeze
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.PowerManager
//import io.mdp43140.superfreeze.badPackages
//import io.mdp43140.superfreeze.goodPackages
class AppListItems(private val ctx: Context){
	var appList: List<AppItem> = emptyList()
	// TODO: should sync across instances, but putting this on companion fucked with compile error :(
	var usageStatsMap: Map<String, UsageStats>? = null
	abstract class AbstractItem {
		abstract var flags: Int
		abstract var icon: Drawable?
		abstract var isInstalledByFDroid: Boolean
		abstract var isItemSelected: Boolean
		abstract var isSignedByFDroid: Boolean
		abstract var ignoreRunning: Boolean
		abstract var ignoreBgFree: Boolean
		abstract var label: String
		abstract var pkg: String
		abstract var selected: Boolean
		abstract var stopMode: Int
		abstract fun loadIcon()
	}
	class AppItem(val appInfo: ApplicationInfo, val ctx: Context): AbstractItem(){
		override var flags: Int = appInfo.flags
		override var icon: Drawable? = null
		override var ignoreRunning: Boolean = false
		override var ignoreBgFree: Boolean = true
		override var isInstalledByFDroid: Boolean  = false
		override var isItemSelected: Boolean = false
		override var isSignedByFDroid: Boolean = false
		override var label: String = appInfo.loadLabel(ctx.packageManager).toString()
		override var pkg: String = appInfo.packageName
		override var selected: Boolean = false
		override var stopMode: Int = -1
		override fun loadIcon(){
			icon = appInfo.loadIcon(ctx.packageManager)
		}
	}
	fun loadPrefs(){
		if (prefs == null) prefs = ctx.getSharedPreferences("apps",Context.MODE_PRIVATE)
	}
	fun loadApps(){
		setFromApplicationInfo(ctx.packageManager.getInstalledApplications(PackageManager.GET_META_DATA))
	}
	fun setFromApplicationInfo(appList2: List<ApplicationInfo>){
		appList = appList2.map { AppItem(it,ctx) }
	}
	companion object {
		var prefs: SharedPreferences? = null // apps.xml
	}
}
