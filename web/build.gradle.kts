/*
 * Copyright (c) 2023 toastkidjp.
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
    id("org.jetbrains.kotlin.plugin.serialization") version(libraries.versions.kotlin.get())
    alias(libraries.plugins.composeCompiler)
}

android {
    namespace = "jp.toastkid.web"

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
}

dependencies {
    implementation(project(path = ":data"))
    implementation(project(path = ":lib"))
    implementation(project(path = ":ui"))
    implementation(project(path = ":api"))
    implementation(project(path = ":barcode:library"))
    implementation(libraries.composeMaterial3)
    implementation(libraries.activityCompose)
    implementation("androidx.webkit:webkit:1.12.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:${LibraryVersion.lifecycle}")
    implementation(libraries.lifecycleViewModelCompose)
    implementation(libraries.timber)
    implementation(libraries.jsoup)
    implementation(libraries.kotlinSerialization)

    testImplementation("junit:junit:4.12")
    testImplementation("io.mockk:mockk:${LibraryVersion.mockk}")
    testImplementation("org.robolectric:robolectric:${LibraryVersion.robolectric}")
    testImplementation(testLibraries.bytebuddy)
}