/*
 * SPDX-FileCopyrightText: 2024 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.mdp43140.superfreeze
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.style.CharacterStyle
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
//import io.mdp43140.superfreeze.Constants
import kotlin.system.exitProcess
object CommonFunctions {
	fun isFlagSet(value: Int,flag: Int): Boolean {
		return (value and flag) == flag
	}
	fun isDarkThemeActive(ctx: Context): Boolean {
		return (ctx.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
	}
	fun updateTheme(ctx: Context, prefs: SharedPreferences?){
		val themeStr = ctx.resources.getStringArray(R.array.themes_key)
		AppCompatDelegate.setDefaultNightMode(when (prefs!!.getString("theme",themeStr[0])){
			themeStr[1] -> AppCompatDelegate.MODE_NIGHT_NO
			themeStr[2] -> AppCompatDelegate.MODE_NIGHT_YES
			else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
		})
	}
	fun writeContentToUri(ctx: Context,uri: Uri, content: String){
		ctx.contentResolver.openOutputStream(uri)?.use { outputStream ->
			outputStream.write(content.toByteArray())
		}
	}
	fun highlightText(text: String, highlight: String?, spanHighlightWhat: CharacterStyle): CharSequence {
		if (highlight.isNullOrEmpty()) return text // nothing to highlight
		val valueLower = text.lowercase()
		var offset = 0
		val span = SpannableString(text)
		var index = valueLower.indexOf(highlight, offset)
		while (index >= 0 && offset < valueLower.length){
			span.setSpan(
				spanHighlightWhat,
				index,
				index + highlight.length,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
			)
			offset += index + highlight.length
			index = valueLower.indexOf(highlight, offset)
		}
		return span
	}
	fun ensureNotificationAccessGranted(ctx: Activity){
		MaterialAlertDialogBuilder(ctx)
			.setTitle("Grant notification access")
			.setMessage("Notification access is used to get app notifications (eg. apps that plays media, and apps that has persistent notification)")
			.setPositiveButton(ctx.getString(android.R.string.ok)){ dialogInterface: DialogInterface, _: Int ->
				ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
					addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
					addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
					val showArgs = ComponentName(ctx, NotificationService::class.java).flattenToString()
					val bundle = Bundle().apply { putString(":settings:fragment_args_key", showArgs) }
					putExtra(":settings:fragment_args_key", showArgs)
					putExtra(":settings:show_fragment_args",bundle)
				})
				dialogInterface.dismiss()
			}
			.setNegativeButton(ctx.getString(android.R.string.cancel)){ dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
			.show()
	}
	fun ensureUsageAccessGranted(ctx: Activity){
		MaterialAlertDialogBuilder(ctx)
			.setTitle("Grant usage access")
			.setMessage("Usage statistics is used for last app usage")
			.setPositiveButton(ctx.getString(android.R.string.ok)){ dialogInterface: DialogInterface, _: Int ->
				ctx.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
					addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
					addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
					val showArgs = ComponentName(ctx, App::class.java).flattenToString()
					val bundle = Bundle().apply { putString(":settings:fragment_args_key", showArgs) }
					putExtra(":settings:fragment_args_key", showArgs)
					putExtra(":settings:show_fragment_args",bundle)
				})
				dialogInterface.dismiss()
			}
			.setNegativeButton(ctx.getString(android.R.string.cancel)){ dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
			.show()
	}
}
