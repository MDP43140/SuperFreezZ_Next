/*
 * SPDX-FileCopyrightText: 2025  MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
import java.util.Properties // used by signingConfigs.release (ksProps variable)
import com.android.build.gradle.tasks.PackageAndroidArtifact // used by empty app-metadata.properties

plugins {
	alias(libs.plugins.androidx.baselineprofile)
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
}
kotlin {
	// Used as defaults for android.kotlinOptions.jvmTarget and android.compileOptions.*Compatibility
	jvmToolchain(21)
}
android {
	compileSdk = 35
	buildToolsVersion = "35.0.0"
	namespace = "io.mdp43140.superfreeze"
	defaultConfig {
		applicationId = "io.mdp43140.superfreeze_next"
		minSdk = 24
		targetSdk = compileSdk
		versionCode = 9
		versionName = "1.4.3"
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		testInstrumentationRunnerArguments["disableAnalytics"] = "true"
		vectorDrawables {
			useSupportLibrary = false
		}
	}
	signingConfigs {
		create("main"){
			val ksPropsFile = rootProject.file(".signing/keystore.properties")
			if (ksPropsFile.exists()){
				val ksProps = Properties().apply {
					load(ksPropsFile.inputStream())
				}
				keyAlias = ksProps["keyAlias"] as String
				keyPassword = ksProps["keyPassword"] as String
				storeFile = file(ksProps["storeFile"] as String)
				storePassword = ksProps["storePassword"] as String
			}
		}
	}
	lint {
		abortOnError = false
		checkReleaseBuilds = false // we did thousands of these on debug builds already...
		lintConfig = file("lint.xml")
	}
	buildTypes {
		debug {
			applicationIdSuffix = ".debug"
			isDebuggable = true
		}
		release {
			isMinifyEnabled = true
			isShrinkResources = true
		//isCrunchPngs = true // no longer needed, since the PNGs are optimized in the first place before compiling
			isDebuggable = false
			isProfileable = false
			isJniDebuggable = false
			isPseudoLocalesEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
			signingConfig = signingConfigs.getByName("main")
			vcsInfo.include = false
			postprocessing {
				isRemoveUnusedCode = true
				isRemoveUnusedResources = true
				isObfuscate = false
				isOptimizeCode = true
			}
		}
	}
	androidResources {
		// Allows changing app language through Android settings
		// Does sacrifice couple kilobytes though, but probably worth it
		generateLocaleConfig = true
	}
	buildFeatures {
		buildConfig = true
		compose = false
		viewBinding = true
	}
	packaging {
	// makes the app 1MB bigger :(
		dex {
			useLegacyPackaging = false
		}
		jniLibs {
			useLegacyPackaging = false
		}
		// TODO: how can we get rid of assets/dexopt, and META-INF/com/android/build/gradle/app-metadata.properties ?
		resources {
			excludes += listOf(
				"assets/dexopt/baseline.prof",
				"assets/dexopt/baseline.profm",
				"junit/runner/logo.gif",
				"junit/runner/smalllogo.gif",
				"kotlin/**.kotlin_builtins",
				"META-INF/**", // including com/android/build/gradle/app-metadata.properties, services/**, version-control-info.textproto
				"DebugProbesKt.bin",
				"kotlin-tooling-metadata.json",
				"LICENSE-junit.txt"
			)
		}
	}
	baselineProfile {
		dexLayoutOptimization = true
	}
	dependenciesInfo {
		// https://gitlab.com/IzzyOnDroid/repo/-/issues/491
		includeInApk = false
		includeInBundle = false
	}
	// empty app-metadata.properties (not removing it sadly)
	// https://stackoverflow.com/a/77745844
	tasks.withType<PackageAndroidArtifact> {
		doFirst { appMetadata.asFile.orNull?.writeText("") }
	}
}
dependencies {
	// AndroidX App Compatibility
	implementation(libs.androidx.appcompat)
	// AndroidX Kotlin
	implementation(libs.androidx.kt)
	// Preference
	implementation(libs.androidx.pref.kt)
	// MD3 on different Android versions
	implementation(libs.material)
	// Pull to refresh
	implementation(libs.androidx.swiperefreshlayout)
	// LibSU, for root support
	implementation(libs.su)
	// Leak detection
	debugImplementation(libs.leakcanary.android)
	// Error logger
	implementation(libs.ael.kt)
	// Scroll bar
	implementation(libs.fastscroll)
	// baseline profile
	"baselineProfile"(project(":baselineprofile"))
	// Tests (AndroidJUnitRunner & JUnit Rules, Assertions)
	androidTestImplementation(libs.androidx.test.runner)
	androidTestImplementation(libs.androidx.test.junit)
}
