plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
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
    implementation("androidx.compose.material3:material3:${property.LibraryVersion.composeMaterial3}")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("io.coil-kt:coil-compose:${property.LibraryVersion.coilCompose}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${property.LibraryVersion.lifecycle}")
    implementation("androidx.activity:activity-compose:${property.LibraryVersion.activityCompose}")

    testImplementation("junit:junit:${property.LibraryVersion.junit}")
    testImplementation("io.mockk:mockk:${property.LibraryVersion.mockk}")
}
