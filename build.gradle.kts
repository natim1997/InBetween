
plugins {
    id("com.android.application")         version "8.2.1" apply false
    id("com.android.library")             version "8.2.1" apply false
    id("org.jetbrains.kotlin.android")    version "1.9.10" apply false
    id("com.google.gms.google-services")  version "4.3.15" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
