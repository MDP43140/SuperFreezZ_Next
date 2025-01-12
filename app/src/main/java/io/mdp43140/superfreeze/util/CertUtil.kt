/*
 * SPDX-FileCopyrightText: 2025 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.mdp43140.superfreeze.util
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import io.mdp43140.superfreeze.R
//import io.mdp43140.superfreeze.util.getPackageInfoCompat // CompatUtil.kt
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
object CertUtil {
	private val TAG = "CertUtil"
	private val fDroidPkgNames = hashSetOf<String>(
		"org.fdroid.basic",
		"org.fdroid.fdroid",
		"org.fdroid.fdroid.privileged",
		"com.machiav3lli.fdroid",
		"eu.bubu1.fdroidclassic",
		"in.sunilpaulmathew.izzyondroid",
		"nya.kitsunyan.foxydroid",
		"org.gdroid.gdroid"
	)
	fun isSignedByFDroid(ctx: Context, pkgName: String): Boolean {
		return try {
			getX509Certs(ctx, pkgName).any { cert ->
				cert.subjectDN.name == "CN=FDroid,OU=FDroid,O=fdroid.org,L=ORG,ST=ORG,C=UK"
			}
		} catch (e: Exception){
			false
		}
	}
	fun isInstalledByFDroid(ctx: Context, pkgName: String): Boolean {
		val installer = ctx.packageManager.getInstallerPackageNameCompat(pkgName)
		return installer != null && fDroidPkgNames.contains(installer)
	}
	private fun getX509Certs(ctx: Context, pkgName: String): List<X509Certificate> {
		return try {
			val packageInfo = ctx.packageManager.getPackageInfoCompat(
				pkgName,
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
					PackageManager.GET_SIGNING_CERTIFICATES
				else
					@Suppress("DEPRECATION")
					PackageManager.GET_SIGNATURES
			)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				if (packageInfo.signingInfo!!.hasMultipleSigners())
					packageInfo.signingInfo!!.apkContentsSigners.map { genX509Cert(it) }
				else
					packageInfo.signingInfo!!.signingCertificateHistory.map { genX509Cert(it) }
			} else {
				@Suppress("DEPRECATION")
				packageInfo.signatures!!.map { genX509Cert(it) }
			}
		} catch (e: Exception) {
			emptyList()
		}
	}
	private fun genX509Cert(signature: Signature): X509Certificate {
		return CertificateFactory
			.getInstance("X509")
			.generateCertificate(
				signature
					.toByteArray()
					.inputStream()
			) as X509Certificate
	}
}
