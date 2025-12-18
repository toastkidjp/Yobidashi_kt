/*
 * Copyright (c) 2023 toastkidjp.
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

android {
    namespace = "jp.toastkid.calendar"

    compileSdkVersion(BuildTool.compileSdk)

    defaultConfig {
        minSdkVersion(BuildTool.minSdk)
        namespace = "jp.toastkid.calendar"
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

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":lib"))
    implementation(libraries.composeMaterial3)
    implementation(libraries.coreKtx)
    implementation(libraries.activityCompose)
    implementation(libraries.lifecycleViewModelCompose)
    implementation(libraries.timber)

    testImplementation(testLibraries.junit)
    testImplementation(testLibraries.mockK)
    testImplementation(testLibraries.coroutines)
}

