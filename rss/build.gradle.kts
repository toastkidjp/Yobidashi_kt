/*
 * Copyright (c) 2021 toastkidjp.
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

android {
    namespace = "jp.toastkid.rss"

    compileSdkVersion(BuildTool.compileSdk)

    defaultConfig {
        minSdkVersion(BuildTool.minSdk)
    }

    buildTypes {
        release {
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
    implementation(project(path = ":api"))
    implementation(project(path = ":lib"))

    implementation("androidx.core:core-ktx:${LibraryVersion.ktx}")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${LibraryVersion.coroutines}")

    implementation("androidx.compose.material3:material3:${LibraryVersion.composeMaterial3}")
    implementation("io.coil-kt:coil-compose:${LibraryVersion.coilCompose}")
    implementation("androidx.activity:activity-compose:${LibraryVersion.activityCompose}")

    testImplementation("junit:junit:${LibraryVersion.junit}")
    testImplementation("io.mockk:mockk:${LibraryVersion.mockk}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${LibraryVersion.coroutinesTest}")
}
