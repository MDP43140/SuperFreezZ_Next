/*
 * SPDX-FileCopyrightText: 2024 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.mdp43140.superfreeze
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import io.mdp43140.ael.ErrorLogger
import io.mdp43140.superfreeze.ui.MainActivity
class App: Application(){
	private var runCount = 0
	override fun attachBaseContext(base: Context){
		super.attachBaseContext(base)
		// Catches bugs and crashes, and makes it easy to report the bug
		if (ErrorLogger.instance == null){
			ErrorLogger(base)
			ErrorLogger.instance?.isNotification = true // sends a notification
			ErrorLogger.reportUrl = "https://github.com/mdp43140/SuperFreezZ_Next/issues/new"
			ErrorLogger.instance?.addListener(object: ErrorLogger.OnErrorListener {
				override fun onError(unused: Thread, e: Throwable){
					if (e is IndexOutOfBoundsException && e.message?.contains("Inconsistency detected.") == true){
						Toast.makeText(base,"Inconsistency detected (3rd-party library bug), nothing we can do other than reopening the activity",Toast.LENGTH_SHORT).show();
						base.startActivity(Intent(base,MainActivity::class.java).apply {
							addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
							addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
						})
					}
				}
			})
		}
	}
	override fun onCreate(){
		super.onCreate()
		prefs = PreferenceManager.getDefaultSharedPreferences(this)
		// Stores how many times the app has been opened,
		// Can also be used for first run related codes in the future, who knows...
		runCount = prefs!!.getInt("runCount",0)
		prefs!!.edit().putInt("runCount",runCount + 1).apply()
		// Update theme and apply dynamic color
		CommonFunctions.updateTheme(this,prefs)
		if (prefs!!.getBoolean("dynamicColor",true)){
			DynamicColors.applyToActivitiesIfAvailable(this)
			DynamicColors.wrapContextIfAvailable(this)
		}
	}
	companion object {
		var appListItems: AppListItems? = null
		var prefs: SharedPreferences? = null
		var workMode: String? = null
	}
}
