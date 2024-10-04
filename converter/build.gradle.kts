/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    alias(libraries.plugins.composeCompiler)
}

android {
    namespace = "jp.toastkid.converter"
    compileSdk = property.BuildTool.compileSdk

    defaultConfig {
        minSdk = 24
        targetSdk = property.BuildTool.compileSdk
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = property.LibraryVersion.composeCompiler
    }
}

dependencies {
    implementation("androidx.compose.material3:material3:${property.LibraryVersion.composeMaterial3}")
    implementation("androidx.core:core-ktx:1.7.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:${property.LibraryVersion.mockk}")
}

