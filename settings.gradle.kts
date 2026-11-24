pluginManagement {
    repositories {
        maven { url = uri("https://maven.cefrium.com") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://maven.cefrium.com") }
        google()
        mavenCentral()
    }
}
rootProject.name = "LastHavenLauncher"
include(":app")
