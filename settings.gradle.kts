pluginManagement {
	repositories {
		google {
			content {
				includeGroupByRegex("com\\.android.*")
				includeGroupByRegex("com\\.google.*")
				includeGroupByRegex("androidx.*")
			}
		}
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
//includeBuild("../ael"){
//	dependencySubstitution {
//		substitute(module("com.github.mdp43140.ael:ael_kt")).using(project(":ael_kt"))
//	}
//}

rootProject.name = "SuperFreezZ Next"
include(":app",":baselineprofile")
