plugins {
	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.android.test) apply false // optional, baselineprofile dependency
	alias(libs.plugins.androidx.baselineprofile) apply false // optional
}
tasks.withType(JavaCompile::class.java){
	options.compilerArgs.add("-Xlint:all")
}
tasks.register<Delete>("clean"){
	delete(rootProject.layout.buildDirectory)
}
