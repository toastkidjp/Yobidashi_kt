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
    namespace = "jp.toastkid.todo"

    compileSdkVersion(BuildTool.compileSdk)

    defaultConfig {
        minSdkVersion(BuildTool.minSdk)
        vectorDrawables.useSupportLibrary = true
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

tasks.withType<GenerateBuildConfig> {
    isEnabled = false
}

dependencies {
    implementation(project(path = ":data"))
    implementation(project(path = ":lib"))

    implementation(libraries.coreKtx)

    implementation(libraries.composeMaterial3)
    implementation(libraries.pagingCompose)
    implementation(libraries.lifecycleRuntimeKtx)
    implementation(libraries.lifecycleViewModelCompose)
    implementation(libraries.activityCompose)

    implementation(libraries.timber)
    implementation(libraries.coroutines)

    testImplementation("junit:junit:${LibraryVersion.junit}")
    testImplementation("io.mockk:mockk:${LibraryVersion.mockk}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${LibraryVersion.coroutinesTest}")
    testImplementation(testLibraries.bytebuddy)
}
