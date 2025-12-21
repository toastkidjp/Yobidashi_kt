/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
import property.BuildTool

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("jacoco.definition")
    alias(libraries.plugins.composeCompiler)
}

// TODO apply(from = "../jacoco.gradle.kts")

android {
    namespace = "jp.toastkid.loan"

    compileSdkVersion(BuildTool.compileSdk)

    defaultConfig {
        minSdkVersion(BuildTool.minSdk)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(path = ":lib"))
    implementation(libraries.activityCompose)

    implementation(libraries.coroutines)

    implementation(libraries.composeMaterial3)

    testImplementation(testLibraries.junit)
    testImplementation(testLibraries.mockK)
}