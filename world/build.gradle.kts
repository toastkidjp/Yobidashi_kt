plugins {
    id("com.android.library")
    id("kotlin-android")
    alias(libraries.plugins.composeCompiler)
}

android {
    namespace = "jp.toastkid.world"

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

    testImplementation("junit:junit:${property.LibraryVersion.junit}")
    testImplementation("io.mockk:mockk:${property.LibraryVersion.mockk}")
}
