pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        // Android Gradle Plugin
        id("com.android.application")         version "8.2.1" apply false
        id("com.android.library")             version "8.2.1" apply false

        // Kotlin Android (תואם ל-Compose Compiler 1.5.3)
        id("org.jetbrains.kotlin.android")    version "1.9.10" apply false

        // Google Services (Firebase)
        id("com.google.gms.google-services")  version "4.3.15" apply false
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "InBetween"
include(":app")
