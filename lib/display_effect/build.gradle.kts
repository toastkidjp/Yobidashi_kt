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
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libraries.coreKtx)
    testImplementation("junit:junit:4.13.2")
}
