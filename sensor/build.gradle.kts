plugins {
    id("com.android.library")
    id("kotlin-android")
    alias(libraries.plugins.composeCompiler)
}

android {
    namespace = "jp.toastkid.sensor"

    compileSdkVersion(property.BuildTool.compileSdk)

    defaultConfig {
        minSdkVersion(property.BuildTool.minSdk)
    }

    buildFeatures {
        compose = true
    }
}

tasks.withType<com.android.build.gradle.tasks.GenerateBuildConfig> {
    isEnabled = false
}

dependencies {
    implementation(project(":lib"))
    implementation(project(":ui"))
    implementation(libraries.composeMaterial3)
    implementation(libraries.coreKtx)
    implementation(libraries.lifecycleRuntimeKtx)
    implementation(libraries.lifecycleViewModelCompose)
    implementation(libraries.activityCompose)

    testImplementation(testLibraries.junit)
    testImplementation(testLibraries.mockK)
}
