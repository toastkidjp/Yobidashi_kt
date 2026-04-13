import property.BuildTool

plugins {
    id("com.android.library")
}

android {
    compileSdkVersion(BuildTool.compileSdk)

    namespace = "jp.toastkid.display.effect"

    defaultConfig {
        minSdk = BuildTool.minSdk
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(libraries.coreKtx)
    testImplementation("junit:junit:4.13.2")
}
