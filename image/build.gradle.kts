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
    namespace = "jp.toastkid.image"

    compileSdkVersion(BuildTool.compileSdk)

    defaultConfig {
        minSdk = BuildTool.minSdk
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

tasks.withType<GenerateBuildConfig> {
    isEnabled = false
}

dependencies {
    implementation(project(":lib"))
    implementation(project(":ui"))

    // Compose dependencies.
    implementation(libraries.composeMaterial3)
    implementation(libraries.lifecycleRuntimeKtx)
    implementation(libraries.activityCompose)
    implementation(libraries.composeAnimation)

    implementation(libraries.coreKtx)
    implementation(libraries.exifinterface)

    implementation(libraries.timber)
    implementation(libraries.coroutines)

    testImplementation(testLibraries.junit)
    testImplementation(testLibraries.mockK)
    testImplementation(testLibraries.bytebuddy)
    testImplementation(testLibraries.robolectric)
    testImplementation(testLibraries.coroutines)
}