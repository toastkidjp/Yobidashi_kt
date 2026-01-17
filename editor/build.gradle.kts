/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
import com.android.build.gradle.tasks.GenerateBuildConfig
import property.BuildTool

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("jacoco.definition")
    alias(libraries.plugins.composeCompiler)
}

// TODO apply from: '../jacoco.gradle'
// TODO apply from: '../detekt.gradle'

android {
    namespace = "jp.toastkid.editor"

    compileSdkVersion(BuildTool.compileSdk)

    defaultConfig {
        minSdkVersion(BuildTool.minSdk)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
    }
}

tasks.withType<GenerateBuildConfig> {
    isEnabled = false
}

dependencies {
    implementation(project(":lib"))
    implementation(project(":ui"))

    implementation(libraries.coreKtx)

    // Compose dependencies.
    implementation(libraries.composeFoundation)
    implementation(libraries.composeMaterial3)
    implementation(libraries.activityCompose)
    implementation(libraries.lifecycleRuntimeKtx)
    implementation(libraries.lifecycleViewModelCompose)

    implementation(libraries.timber)
    implementation(libraries.jsoup)

    testImplementation(testLibraries.junit)
    testImplementation(testLibraries.mockK)
    testImplementation(testLibraries.robolectric)
}
