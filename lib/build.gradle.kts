/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
import property.BuildTool
import property.LibraryVersion
import com.android.build.gradle.tasks.GenerateBuildConfig
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension

plugins {
  id("com.android.library")
  id("kotlin-android")
    id("jacoco")
}
//TODO apply from: '../jacoco.gradle'

android {
    compileSdkVersion(BuildTool.compileSdk)

    defaultConfig {
        minSdkVersion(BuildTool.minSdk)
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "${LibraryVersion.composeCompiler}"
    }
}

tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
    }
}

tasks.withType<GenerateBuildConfig> {
    isEnabled = false
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${BuildTool.kotlinVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${LibraryVersion.coroutines}")
    implementation("androidx.core:core-ktx:${LibraryVersion.ktx}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${LibraryVersion.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${LibraryVersion.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${LibraryVersion.lifecycle}")

    implementation("androidx.compose.material3:material3:${LibraryVersion.composeMaterial3}")

    implementation("com.jakewharton.timber:timber:${LibraryVersion.timber}")

    testImplementation("junit:junit:${LibraryVersion.junit}")
    testImplementation("org.robolectric:robolectric:${LibraryVersion.robolectric}")
    testImplementation("io.mockk:mockk:${LibraryVersion.mockk}")
}
