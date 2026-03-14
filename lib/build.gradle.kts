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
//TODO apply from: '../jacoco.gradle'

android {
    namespace = "jp.toastkid.lib"

    compileSdkVersion(BuildTool.compileSdk)

    defaultConfig {
        minSdk = BuildTool.minSdk
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_21)
        targetCompatibility(JavaVersion.VERSION_21)
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    buildFeatures {
        compose = true
    }
}

tasks.withType<GenerateBuildConfig> {
    isEnabled = false
}

dependencies {
    implementation(libraries.coroutines)
    implementation(libraries.coreKtx)
    implementation(libraries.lifecycleViewModelKtx)
    implementation(libraries.lifecycleRuntimeKtx)

    implementation(libraries.composeUi)
    implementation(libraries.composeMaterial3)

    implementation(libraries.timber)

    testImplementation(testLibraries.junit)
    testImplementation(testLibraries.robolectric)
    testImplementation(testLibraries.mockK)
    testImplementation(testLibraries.bytebuddy)
}
