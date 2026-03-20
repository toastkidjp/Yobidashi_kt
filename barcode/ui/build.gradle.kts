/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
import com.android.build.gradle.tasks.GenerateBuildConfig
import property.BuildTool

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlinx.kover")
    alias(libraries.plugins.composeCompiler)
}

// TODO apply from: '../jacoco.gradle'

android {
    namespace = "jp.toastkid.barcode.ui"

    compileSdkVersion(BuildTool.compileSdk)

    defaultConfig {
        minSdk = BuildTool.minSdk
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
    implementation(project(":barcode:library"))

    implementation(libraries.coreKtx)

    // Compose dependencies.
    implementation(libraries.composeMaterial3)
    implementation(libraries.lifecycleRuntimeKtx)
    implementation(libraries.activityCompose)

    implementation("androidx.camera:camera-camera2:${libraries.versions.cameraX.get()}")
    implementation("androidx.camera:camera-lifecycle:${libraries.versions.cameraX.get()}")
    implementation("androidx.camera:camera-compose:${libraries.versions.cameraX.get()}")

    implementation(libraries.timber)

    testImplementation(testLibraries.junit)
    testImplementation(testLibraries.mockK)
}
