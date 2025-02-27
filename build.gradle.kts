plugins {
	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.android.test) apply false // optional, baselineprofile dependency
	alias(libs.plugins.androidx.baselineprofile) apply false // optional
	alias(libs.plugins.kotlin.android) apply false
}
tasks.withType(JavaCompile::class.java){
	options.compilerArgs.add("-Xlint:all")
}
tasks.register("clean",Delete::class){
	delete(rootProject.layout.buildDirectory)
}
