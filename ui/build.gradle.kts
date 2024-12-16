/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
import property.BuildTool
import property.LibraryVersion

plugins {
    id("com.android.library")
    id("kotlin-android")
    alias(libraries.plugins.composeCompiler)
}

android {
    namespace = "jp.toastkid.ui"

    compileSdkVersion(BuildTool.compileSdk)

    defaultConfig {
        minSdkVersion(BuildTool.minSdk)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = LibraryVersion.composeCompiler
    }
}

dependencies {
    implementation(project(path = ":lib"))
    implementation(libraries.composeUi)
    implementation(libraries.coilCompose)
    implementation("androidx.compose.material3:material3:${LibraryVersion.composeMaterial3}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${LibraryVersion.lifecycle}")
    testImplementation("junit:junit:4.12")
}