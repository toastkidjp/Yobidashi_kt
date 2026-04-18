/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
import property.BuildTool

plugins {
    id("com.android.library")
    alias(libraries.plugins.composeCompiler)
}

android {
    namespace = "jp.toastkid.ui"

    compileSdkVersion(BuildTool.compileSdk)

    defaultConfig {
        minSdk = BuildTool.minSdk
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

dependencies {
    implementation(project(path = ":lib"))
    implementation(libraries.composeUi)
    implementation(libraries.composeFoundation)
    implementation(libraries.coilCompose)
    implementation(libraries.coilGif)
    implementation(libraries.coilNetwork)
    implementation(libraries.composeMaterial3)
    implementation(libraries.lifecycleViewModelCompose)
    implementation("androidx.navigation:navigation-compose:2.9.2")
    implementation("androidx.navigation:navigation-runtime-ktx:2.9.2")
    api("androidx.navigation:navigation-compose:2.9.2")

    testImplementation("junit:junit:4.12")
}