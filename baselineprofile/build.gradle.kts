@file:Suppress("UnstableApiUsage")
import com.android.build.api.dsl.ManagedVirtualDevice
plugins {
	id("com.android.test")
	id("org.jetbrains.kotlin.android")
	id("androidx.baselineprofile")
}
android {
	buildToolsVersion = "35.0.0"
	compileSdk = 35
	namespace = "io.mdp43140.baselineprofile"
	java {
		toolchain {
			languageVersion = JavaLanguageVersion.of(21)
		}
	}
	kotlin {
		jvmToolchain(21)
	}
	defaultConfig {
		minSdk = 24
		targetSdk = compileSdk
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}
	targetProjectPath = ":app"
	// This code creates the gradle managed device used to generate baseline profiles.
//testOptions.managedDevices.devices {
//	create<ManagedVirtualDevice>("pixel8Api34") {
//		device = "Pixel 8"
//		apiLevel = 34
//		systemImageSource = "aosp"
//	}
//}
}
baselineProfile {
	// This is the configuration block for the Baseline Profile plugin.
	// You can specify to run the generators on a managed devices or connected devices.
//managedDevices += "pixel8Api34"
	useConnectedDevices = true
}
androidComponents {
	onVariants { v ->
		val artifactsLoader = v.artifacts.getBuiltArtifactsLoader()
		v.instrumentationRunnerArguments.put(
			"targetAppId",
			v.testedApks.map { artifactsLoader.load(it)?.applicationId!! }
		)
	}
}
dependencies {
	implementation("androidx.test.ext:junit:1.2.1")
	implementation("androidx.test.espresso:espresso-core:3.6.1")
	implementation("androidx.test.uiautomator:uiautomator:2.3.0")
	implementation("androidx.benchmark:benchmark-macro-junit4:1.3.3")
}
