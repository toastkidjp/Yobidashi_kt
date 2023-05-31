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
  id("org.jetbrains.kotlin.kapt")
  id("jacoco.definition")
}
// TODO apply(from = "../jacoco.gradle.kts")

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
    implementation(project(path = ":lib"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${BuildTool.kotlinVersion}")

    implementation("androidx.core:core-ktx:${LibraryVersion.ktx}")
    implementation("androidx.paging:paging-common:${LibraryVersion.paging}")
    implementation("androidx.paging:paging-common-ktx:${LibraryVersion.paging}")

    implementation("androidx.compose.material3:material3:${LibraryVersion.composeMaterial3}")
    implementation("androidx.paging:paging-compose:${LibraryVersion.pagingCompose}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${LibraryVersion.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${LibraryVersion.lifecycle}")
    implementation("androidx.compose.runtime:runtime-livedata:${LibraryVersion.compose}")
    implementation("androidx.activity:activity-compose:1.4.0")

    implementation("com.jakewharton.timber:timber:${LibraryVersion.timber}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${LibraryVersion.coroutines}")

    implementation("androidx.room:room-runtime:${LibraryVersion.room}")
    implementation("androidx.room:room-paging:${LibraryVersion.room}")
    kapt("androidx.room:room-compiler:${LibraryVersion.room}")

    testImplementation("junit:junit:${LibraryVersion.junit}")
    testImplementation("io.mockk:mockk:${LibraryVersion.mockk}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${LibraryVersion.coroutinesTest}")
}
