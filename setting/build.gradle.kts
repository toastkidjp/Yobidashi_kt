plugins {
    id("com.android.library")
    id("kotlin-android")
    alias(libraries.plugins.composeCompiler)
}

android {
    namespace = "jp.toastkid.setting"

    compileSdkVersion(property.BuildTool.compileSdk)

    defaultConfig {
        minSdkVersion(property.BuildTool.minSdk)
    }

    buildTypes {
        release {
        }
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

tasks.withType<com.android.build.gradle.tasks.GenerateBuildConfig> {
    isEnabled = false
}

dependencies {
    implementation(project(":lib"))
    implementation(project(":data"))
    implementation(project(":calendar"))
    implementation(project(":web"))
    implementation(project(":ui"))
    implementation(project(":search"))
    implementation(project(":editor"))

    implementation(libraries.composeMaterial3)
    implementation(libraries.coreKtx)
    implementation(libraries.activityCompose)
    implementation(libraries.lifecycleViewModelCompose)
    implementation(libraries.exifinterface)
    implementation(libraries.timber)
    implementation("com.godaddy.android.colorpicker:compose-color-picker:0.7.0")

    testImplementation(testLibraries.junit)
    testImplementation(testLibraries.mockK)
    testImplementation(testLibraries.coroutines)
    testImplementation(testLibraries.bytebuddy)
}
