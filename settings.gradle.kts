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

rootProject.name = "Bíblia Sagrada"
include(":app")
include(":core:common")
include(":core:database")
include(":core:network")
include(":core:ui")
include(":feature:bible:bridge")
include(":feature:bible:impl")
