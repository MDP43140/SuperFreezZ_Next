@file:Suppress("UnstableApiUsage")
import com.android.build.api.dsl.ManagedVirtualDevice
plugins {
	alias(libs.plugins.android.test)
	alias(libs.plugins.androidx.baselineprofile)
	alias(libs.plugins.kotlin.android)
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
	implementation(libs.androidx.benchmark.macroJunit4)
	implementation(libs.androidx.test.junit)
	implementation(libs.androidx.test.espresso)
	implementation(libs.androidx.test.uiautomator)
}
