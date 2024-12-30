/*
 * SPDX-FileCopyrightText: 2024 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.mdp43140.superfreeze.util
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PackageInfoFlags
import android.os.Build

// TODO: uhh can we avoid using Throws?
@Throws(Exception::class)
fun PackageManager.getPackageInfoCompat(pkg: String, flags: Int = 0): PackageInfo {
	return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
		getPackageInfo(pkg, PackageInfoFlags.of(flags.toLong()))
	else
		getPackageInfo(pkg, flags)
}
fun PackageManager.getInstallerPackageNameCompat(pkg: String): String? {
	return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
		getInstallSourceInfo(pkg).installingPackageName
	else
		getInstallerPackageName(pkg)
}
