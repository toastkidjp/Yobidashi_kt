/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
import property.BuildTool

plugins {
    id("org.jetbrains.kotlin.plugin.serialization") version(libraries.versions.kotlin.get())
    id("com.android.library")
    id("kotlin-android")
    id("jacoco.definition")
}

android {
    namespace = "jp.toastkid.api"

    compileSdkVersion(BuildTool.compileSdk)

    defaultConfig {
        minSdkVersion(BuildTool.minSdk)
        namespace = "jp.toastkid.api"
    }

    buildTypes {
        release {
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libraries.coreKtx)
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation(libraries.timber)
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation(libraries.jsoup)
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation(libraries.kotlinSerialization)

    testImplementation(testLibraries.junit)
    testImplementation(testLibraries.mockK)
    testImplementation(testLibraries.coroutines)
    testImplementation(testLibraries.bytebuddy)
}

