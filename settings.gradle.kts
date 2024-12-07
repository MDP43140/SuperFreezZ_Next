pluginManagement {
	repositories {
		google()
		mavenCentral()
		gradlePluginPortal()
	}
}
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		google()
		mavenCentral()
		maven("https://jitpack.io")
	}
}
// Use this to override dependency to use local ones instead
//includeBuild("../ael"){
//	dependencySubstitution {
//		substitute(module("com.github.mdp43140.ael:ael_kt")).using(project(":ael_kt"))
//	}
//}

rootProject.name = "Template app"
include(":app",":baselineprofile")
