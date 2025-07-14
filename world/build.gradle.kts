import property.LibraryVersion

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
    composeOptions {
        kotlinCompilerExtensionVersion = "${property.LibraryVersion.composeCompiler}"
    }
}

tasks.withType<com.android.build.gradle.tasks.GenerateBuildConfig> {
    isEnabled = false
}

dependencies {
    implementation(project(":lib"))
    implementation(project(":ui"))
    implementation(libraries.composeMaterial3)
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${property.LibraryVersion.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${LibraryVersion.lifecycle}")
    implementation(libraries.activityCompose)

    testImplementation("junit:junit:${property.LibraryVersion.junit}")
    testImplementation("io.mockk:mockk:${property.LibraryVersion.mockk}")
}
