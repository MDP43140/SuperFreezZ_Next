/*
 * SPDX-FileCopyrightText: 2024 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.example
import android.content.Context
import android.content.res.Configuration
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import java.text.DecimalFormat
//import com.example.Constants
import kotlin.system.exitProcess
object CommonFunctions {
	fun convertSize(length: Long): String {
		val format = DecimalFormat("#.##")
		val kib:Long = 1024
		val mib:Long = 1048576
		return if (length > mib) {
			format.format(length / mib) + " MB"
		} else if (length > kib) {
			format.format(length / kib) + " KB"
		} else {
			format.format(length) + " B"
		}
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
}