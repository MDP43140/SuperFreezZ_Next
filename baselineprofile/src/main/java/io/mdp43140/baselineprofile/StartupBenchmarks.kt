package io.mdp43140.baselineprofile
import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
/**
 * This class benchmarks the app startup speed.
 * Run this benchmark to verify how effective a Baseline Profile is.
 * It does this by comparing [CompilationMode.None] and [CompilationMode.Partial],
 * which represents the app with no Baseline and uses Baseline Profiles.
 *
 * Run this benchmark to see startup measurements and captured system traces for verifying
 * the effectiveness of your Baseline Profiles. You can run it directly from Android
 * Studio as an instrumentation test, or run all benchmarks for a variant, for example benchmarkRelease,
 * with this Gradle task:
 * gradle :baselineprofile:connectedBenchmarkReleaseAndroidTest
 *
 * For more info, see https://d.android.com/macrobenchmark#create-macrobenchmark
 * and https://d.android.com/topic/performance/benchmarking/macrobenchmark-instrumentation-args
 **/
@RunWith(AndroidJUnit4::class)
@LargeTest
class StartupBenchmarks {

	@get:Rule
	val rule = MacrobenchmarkRule()

	@Test
	fun startupCompilationNone() =
		benchmark(CompilationMode.None())

	@Test
	fun startupCompilationBaselineProfiles() =
		benchmark(CompilationMode.Partial(BaselineProfileMode.Require))

	private fun benchmark(compilationMode: CompilationMode) {
		// The application id for the running build variant is read from the instrumentation arguments.
		rule.measureRepeated(
			packageName = InstrumentationRegistry.getArguments().getString("targetAppId")
				?: throw Exception("targetAppId not passed as instrumentation runner arg"),
			metrics = listOf(StartupTimingMetric()),
			compilationMode = compilationMode,
			startupMode = StartupMode.COLD,
			iterations = 10,
			setupBlock = {
				pressHome()
			},
			measureBlock = {
				startActivityAndWait()
				// The app is fully drawn when Activity.reportFullyDrawn is called.
				// Check the UiAutomator documentation for more
				// information on how to interact with the app.
				// https://d.android.com/training/testing/other-components/ui-automator
			}
		)
	}
}