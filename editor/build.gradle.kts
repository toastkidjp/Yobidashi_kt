/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
import com.android.build.gradle.tasks.GenerateBuildConfig
import property.BuildTool
import property.LibraryVersion

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("jacoco.definition")
}

// TODO apply from: '../jacoco.gradle'
// TODO apply from: '../detekt.gradle'

android {
    compileSdkVersion(BuildTool.compileSdk)

    defaultConfig {
        minSdkVersion(BuildTool.minSdk)
    }

    buildTypes {
        release {
            isMinifyEnabled = true
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = LibraryVersion.composeCompiler
    }
}

tasks.withType<GenerateBuildConfig> {
    isEnabled = false
}

dependencies {
    implementation(project(":lib"))
    implementation(project(":ui"))

    implementation("androidx.core:core-ktx:${LibraryVersion.ktx}")

    // Compose dependencies.
    implementation("androidx.compose.material3:material3:${LibraryVersion.composeMaterial3}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${LibraryVersion.lifecycle}")
    implementation("androidx.activity:activity-compose:${LibraryVersion.activityCompose}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${LibraryVersion.lifecycle}")

    implementation("com.jakewharton.timber:timber:${LibraryVersion.timber}")
    implementation("org.jsoup:jsoup:${LibraryVersion.jsoup}")

    testImplementation("junit:junit:${LibraryVersion.junit}")
    testImplementation("io.mockk:mockk:${LibraryVersion.mockk}")
    testImplementation("org.robolectric:robolectric:${LibraryVersion.robolectric}")
}
