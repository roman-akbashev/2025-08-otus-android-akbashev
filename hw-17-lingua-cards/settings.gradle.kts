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
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "LinguaCards"
include(":core:model")
include(":core:domain")
include(":core:database")
include(":core:network")
include(":core:data")
include(":features:decklist")
include(":features:deckdetail")
include(":features:cardedit")
include(":features:study")
include(":app")
include(":detekt-rules")