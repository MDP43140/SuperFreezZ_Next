/*
 * SPDX-FileCopyrightText: 2018-2020 Hocuri
 * SPDX-FileCopyrightText: 2025 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.mdp43140.superfreeze
import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import androidx.annotation.RequiresApi
import io.mdp43140.superfreeze.util.compareText // AccessibilityNodeInfoExtension.kt
import io.mdp43140.superfreeze.util.matchNode // AccessibilityNodeInfoExtension.kt
import io.mdp43140.superfreeze.util.performClick // AccessibilityNodeInfoExtension.kt
// TODO:
// - On ColorOS, force stop is greyed out if going too fast
// - On ColorOS, not all apps is killed (some is skipped)
// - If force stop button is disabled, press back and abort
//   and after finished it wont restart
class FreezeService: AccessibilityService(){
	private lateinit var forceStopButtonName: String
	private enum class Step {
		DO_NOTHING,
		PRESS_FORCE_STOP,
		PRESS_OK,
		PRESS_BACK
	}
	override fun onCreate(){
		super.onCreate()
		disableIfUsingRoot()
		isEnabled = true
		try {
			val res = applicationContext.packageManager.getResourcesForApplication(AppListItems.settingsPkg)
			val resId = res.getIdentifier("force_stop", "string", AppListItems.settingsPkg)
			if (resId > 0) {
				forceStopButtonName = res.getString(resId)
				Log.i(TAG, "Found force stop button name: ${forceStopButtonName}")
			} else {
				forceStopButtonName = applicationContext.getString(R.string.force_stop)
				Log.e(TAG, "Label for the force stop button in settings could not be found, using fallback: ${forceStopButtonName}")
			}
		} catch (e: PackageManager.NameNotFoundException) {
			Log.e(TAG, "Settings Activity's resources not found")
		}
	}
	@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	override fun onAccessibilityEvent(event: AccessibilityEvent) {
		// We are only interested in WINDOW_STATE_CHANGED events
		if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || event.source == null) return
		Thread.sleep(delayAfterStepDuration)
		when (nextStep){
			Step.DO_NOTHING -> {}
			Step.PRESS_FORCE_STOP -> {
				if (event.className == "${AppListItems.settingsPkg}.applications.InstalledAppDetailsTop" ||
						event.className == "${AppListItems.settingsPkg}.spa.SpaActivity") {
					// 2nd name is newer android settings app info?
					// seen on Android 14 (probably on Android 13 too, but didnt check)
					pressForceStopButton(event.source!!)
				} else {
					Log.w(TAG, "awaited InstalledAppDetails to be the next screen but got ${event.className}")
					wrongScreenShown()
				}
			}
			Step.PRESS_OK -> {
				if (event.className?.let {
					it == "androidx.compose.ui.window.DialogWrapper" ||
					it.endsWith(".app.AlertDialog") ||
					it.endsWith(".app.COUIAlertDialog")
				} == true) {
					pressOkButton(event.source!!)
				} else {
					Log.w(TAG, "awaited AlertDialog to be the next screen but got ${event.className}")
					wrongScreenShown()
				}
			}
			Step.PRESS_BACK -> {
				pressBackButton()
			}
		}
	}
	override fun onInterrupt() {
	}
	private fun wrongScreenShown() {
		// If the last step was more than 8 seconds ago, something went wrong and we should stop to not destroy anything.
		if (System.currentTimeMillis() - lastStepTimestamp > 8000) {
				Log.e(TAG,
					"An unexpected screen turned up and the last step was more than 8 seconds ago. Something went wrong. Aborted to not destroy anything"
				)
			stopAnyCurrentFreezing() // Stop everything, it is to late to do anything :-(
		}
		// else do nothing and simply wait for the next screen to show up.
	}
	private fun pressForceStopButton(node: AccessibilityNodeInfo) {
		notifyThereIsStillMovement(this)
		val button = node.matchNode { nodeInfo: AccessibilityNodeInfo? ->
			arrayListOf<String>(forceStopButtonName).any { text -> nodeInfo?.compareText(text) == true }
		}
		if (button?.isEnabled == false){
			Log.w(TAG,"ROM specific issue (force stop disabled on ColorOS) detected, doing workarounds")
			finishedFreezing()
			pressBackButton()
		}
		else if (button?.performClick() == true) nextStep = Step.PRESS_OK
	}
	private fun pressOkButton(node: AccessibilityNodeInfo) {
		notifyThereIsStillMovement(this)
		// If no OK button found, find Force Stop button
		// i did this because apparently on some ROM "Force stop" button shows first before "OK"
		val button = node.matchNode { nodeInfo: AccessibilityNodeInfo? ->
			nodeInfo?.compareText(getString(android.R.string.ok)) == true
		} ?: node.matchNode { nodeInfo: AccessibilityNodeInfo? ->
			// Some ROMs had different label, see https://gitlab.com/SuperFreezZ/SuperFreezZ/issues/43
			nodeInfo?.compareText(forceStopButtonName) == true
		}
		if (button?.performClick() == true) nextStep = Step.PRESS_BACK
	}
	private fun pressBackButton() {
		finishedFreezing()
		appsToStop -= 1
		performGlobalAction(GLOBAL_ACTION_BACK)
	}
	@RequiresApi(Build.VERSION_CODES.P)
	private fun lockScreen(){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
			Log.i(TAG, "Locking screen")
			performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
		} else {
			Log.i(TAG, "Unable to lock screen because Android version is below 9")
		}
	}
	@RequiresApi(Build.VERSION_CODES.N)
	private fun disableIfUsingRoot(): Boolean {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && App.workMode == "root"){
			Log.i(TAG, "Root method is being used, accessibility service will be turned off automatically to save battery")
			isEnabled = false
			disableSelf()
			return true
		}
		return false
	}
	override fun onServiceConnected() {
		disableIfUsingRoot()
		Log.i(TAG, "Service connected")
		isEnabled = true
	}
	override fun onDestroy() {
		Log.i(TAG, "Service destroyed")
		isEnabled = false
		stopAnyCurrentFreezing()
	}
	fun stopAccessibilityService(){
		// TODO: how were supposed to access this from SettingsActivity? :(
		disableSelf()
	}
	companion object {
		val TAG = "FreezeService" //this::class.simpleName (this shit just puts out long nonsense bruh)
		var onAppCouldNotBeFrozen: ((Context) -> Unit)? = null
		private var nextStep = Step.DO_NOTHING
		private var lastStepTimestamp = 0L
		private var appsToStop = 0
		var delayAfterStepDuration: Long = 0
		var isEnabled = false
			private set
		private val timeoutHandler = Handler(Looper.getMainLooper())
		fun clickFreezeButtons(ctx: Context, amountOfApps: Int) {
			if (!isBusy()) {
				delayAfterStepDuration = if (App.prefs!!.getBoolean("accessibilitySvc_delayAfterStepDetected",false)) 250 else 0
				nextStep = Step.PRESS_FORCE_STOP
				appsToStop = amountOfApps
				notifyThereIsStillMovement(ctx)
			}
		}
		fun finishedFreezing() {
			nextStep = if (appsToStop == 0) Step.DO_NOTHING else Step.PRESS_FORCE_STOP
			onAppCouldNotBeFrozen = null
			timeoutHandler.removeCallbacksAndMessages(null)
		}
		fun isBusy() = (nextStep != Step.DO_NOTHING)
		private fun notifyThereIsStillMovement(ctx: Context) {
			timeoutHandler.removeCallbacksAndMessages(null)
			// After 4 seconds, assume that something went wrong
			timeoutHandler.postDelayed({
				Log.w(TAG, "timeout")
				stopAnyCurrentFreezing()
				onAppCouldNotBeFrozen?.invoke(ctx)
			}, 4000)
			lastStepTimestamp = System.currentTimeMillis()
		}
		fun stopAnyCurrentFreezing(){
			if (isBusy()) Log.i(TAG, "Stopping current freeze process (stopAnyCurrentFreezing()), nextStep was $nextStep")
			nextStep = Step.DO_NOTHING
			timeoutHandler.removeCallbacksAndMessages(null)
		}
	}
}
