/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
import com.android.build.gradle.tasks.GenerateBuildConfig
import property.BuildTool
import property.LibraryVersion

plugins {
  id("com.android.library")
  id("kotlin-android")
  id("jacoco.definition")
  alias(libraries.plugins.composeCompiler)
}
// TODO apply(from = "../jacoco.gradle.kts")

android {
    namespace = "jp.toastkid.pdf"

    compileSdkVersion(BuildTool.compileSdk)

    defaultConfig {
        minSdkVersion(BuildTool.minSdk)
    }

    buildTypes {
        release {
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = LibraryVersion.composeCompiler
    }
}

tasks.withType<GenerateBuildConfig> {
    isEnabled = false
}

dependencies {
    implementation(project(path = ":lib"))
    implementation(project(path = ":ui"))

    implementation("androidx.core:core-ktx:${LibraryVersion.ktx}")

    implementation("androidx.compose.material3:material3:${LibraryVersion.composeMaterial3}")
    implementation(libraries.coilCompose)
    implementation(libraries.activityCompose)

    testImplementation("junit:junit:${LibraryVersion.junit}")
    testImplementation("io.mockk:mockk:${LibraryVersion.mockk}")
}
