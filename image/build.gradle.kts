/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
import property.*
import com.android.build.gradle.tasks.GenerateBuildConfig

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("org.jetbrains.kotlin.kapt")
}

// TODO apply from: '../jacoco.gradle'

android {
    compileSdkVersion(BuildTool.compileSdk)

    defaultConfig {
        minSdkVersion(BuildTool.minSdk)
        vectorDrawables.useSupportLibrary = true
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${BuildTool.kotlinVersion}")

    // Compose dependencies.
    implementation("androidx.compose.material3:material3:${LibraryVersion.composeMaterial3}")
    implementation("io.coil-kt:coil-compose:${LibraryVersion.coilCompose}")
    implementation("io.coil-kt:coil-gif:${LibraryVersion.coilCompose}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${LibraryVersion.lifecycle}")
    implementation("androidx.compose.runtime:runtime-livedata:${LibraryVersion.compose}")
    implementation("androidx.activity:activity-compose:1.4.0")

    implementation("androidx.core:core-ktx:${LibraryVersion.ktx}")
    implementation("androidx.exifinterface:exifinterface:${LibraryVersion.exifinterface}")

    implementation("com.jakewharton.timber:timber:${LibraryVersion.timber}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${LibraryVersion.coroutines}")

    testImplementation("junit:junit:${LibraryVersion.junit}")
    testImplementation("io.mockk:mockk:${LibraryVersion.mockk}")
    testImplementation("org.robolectric:robolectric:${LibraryVersion.robolectric}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${LibraryVersion.coroutinesTest}")
}