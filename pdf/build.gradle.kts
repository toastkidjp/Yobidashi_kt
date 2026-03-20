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
// TODO apply(from = "../jacoco.gradle.kts")

android {
    namespace = "jp.toastkid.pdf"

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
    implementation(project(path = ":lib"))
    implementation(project(path = ":ui"))

    implementation(libraries.coreKtx)

    implementation(libraries.composeMaterial3)
    implementation(libraries.activityCompose)

    testImplementation(testLibraries.junit)
    testImplementation(testLibraries.mockK)
}
