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
    id("org.jetbrains.kotlinx.kover")
    alias(libraries.plugins.composeCompiler)
}

android {
    namespace = "jp.toastkid.calendar"

    compileSdkVersion(BuildTool.compileSdk)

    defaultConfig {
        minSdk = BuildTool.minSdk
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

