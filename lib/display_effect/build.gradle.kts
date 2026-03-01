import property.BuildTool

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdkVersion(BuildTool.compileSdk)

    namespace = "jp.toastkid.display.effect"

    defaultConfig {
        minSdkVersion(BuildTool.minSdk)
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_21)
        targetCompatibility(JavaVersion.VERSION_21)
    }
}

dependencies {
    implementation(libraries.coreKtx)
    testImplementation("junit:junit:4.13.2")
}
