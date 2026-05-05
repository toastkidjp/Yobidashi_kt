plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.serialization") version(libraries.versions.kotlin.get())
}

android {
    namespace = "jp.toastkid.navigation"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 24
    }

    buildTypes {
        release {
        }
    }
}

dependencies {
    implementation(libraries.kotlinSerialization)

    testImplementation("junit:junit:4.13.2")
}