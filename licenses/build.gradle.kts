/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
import property.BuildTool
import property.LibraryVersion
import com.android.build.gradle.tasks.GenerateBuildConfig

plugins {
  id("com.android.library")
  id("kotlin-android")
  id("com.cookpad.android.plugin.license-tools")
}
//TODO apply(from = "../jacoco.gradle.kts")

android {
    compileSdkVersion(BuildTool.compileSdk)

    defaultConfig {
        minSdkVersion(BuildTool.minSdk)
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

tasks.withType<GenerateBuildConfig> {
    isEnabled = false
}

dependencies {
    implementation(project(path = ":lib"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${BuildTool.kotlinVersion}")
    implementation("androidx.core:core-ktx:${LibraryVersion.ktx}")

    testImplementation("junit:junit:${LibraryVersion.junit}")
    testImplementation("io.mockk:mockk:${LibraryVersion.mockk}")
}
