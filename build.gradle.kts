plugins {
//auto suffixed with .gradle.plugin
	id("androidx.baselineprofile") version "1.3.3" apply false // optional
	id("com.android.application") version "8.7.3" apply false
	id("com.android.test") version "8.7.3" apply false // optional, baselineprofile dependency
	kotlin("android") version "2.1.0" apply false // kotlin("android") == "org.jetbrains.kotlin.android"
}
tasks.withType(JavaCompile::class.java){
	options.compilerArgs.add("-Xlint:all")
}
tasks.register("clean",Delete::class){
	delete(rootProject.layout.buildDirectory)
}