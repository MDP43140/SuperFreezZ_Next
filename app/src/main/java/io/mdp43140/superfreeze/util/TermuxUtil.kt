/*
 * SPDX-FileCopyrightText: 2025-2026 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.mdp43140.superfreeze.util
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
object TermuxUtil {
	private const val TERMUX_PKG = "com.termux"
	private const val RUN_COMMAND_PERMISSION = "com.termux.permission.RUN_COMMAND"
	private const val RUN_COMMAND_SERVICE = "com.termux.app.RunCommandService"
	private const val RUN_COMMAND_ACTION = "com.termux.RUN_COMMAND"
	fun isTermuxInstalled(ctx: Context): Boolean {
		return try {
			ctx.packageManager.getPackageInfo(TERMUX_PKG, 0)
			true
		} catch (_: PackageManager.NameNotFoundException) {
			false
		}
	}
	fun isGrantedTermuxExecCmdPermission(ctx: Context): Boolean {
		return true // TODO
	}
	fun isTermuxGrantedExecCmd(ctx: Context): Boolean {
		if (!isTermuxInstalled(ctx)) return false
		if (!isGrantedTermuxExecCmdPermission(ctx)) return false
		val script = buildString {
			append("whoami")
		}
		return execCmd(ctx, arrayOf("-c",script), background = false) == null
	}
	fun isTermuxHaveRootShell(ctx: Context): Boolean {
		return true // TODO
	}
	/**
	 * Run a command inside Termux via RUN_COMMAND intent.
	 * Returns null on success, or an error message string on failure.
	 */
	fun execCmd(
		ctx: Context,
		cmd: Array<String>,
		prog: String = "/data/data/com.termux/files/usr/bin/bash",
		background: Boolean = true,
	): String? {
		// TODO: get command output? (quite complicated to do, need to set pending Intent)
		return try {
			val intent = Intent(RUN_COMMAND_ACTION).apply {
				component = ComponentName(TERMUX_PKG, RUN_COMMAND_SERVICE)
				putExtra("com.termux.RUN_COMMAND_PATH", prog)
				putExtra("com.termux.RUN_COMMAND_ARGUMENTS", cmd)
				putExtra("com.termux.RUN_COMMAND_BACKGROUND", background)
			}
			ctx.startService(intent)
			null
		} catch (e: SecurityException) {
			"SecurityException, please grant Termux permission and set Termux to allow external apps"
		} catch (e: Exception) {
			e.message ?: "Failed to communicate with Termux"
		}
	}
	fun runTermuxCommands(ctx: Context): String? {
		val script = buildString {
			append("su -c ")
			append("whoami")
		}
		return execCmd(ctx, arrayOf("-c",script), background = false)
	}
	fun requestPermission(ctx: Activity){
		ctx.requestPermissions(arrayOf(
			RUN_COMMAND_PERMISSION
		),1)
	}
}
