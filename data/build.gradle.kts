/*
 * Copyright (c) 2023 toastkidjp.
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
  id("com.google.devtools.ksp")
}
//TODO apply from: '../jacoco.gradle'

android {
    namespace = "jp.toastkid.data"

    compileSdkVersion(BuildTool.compileSdk)

    defaultConfig {
        minSdkVersion(BuildTool.minSdk)
        namespace = "jp.toastkid.data"
    }

    buildTypes {
        release {
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
    }
}

tasks.withType<GenerateBuildConfig> {
    isEnabled = false
}

dependencies {
    implementation(project(":lib"))
    implementation(libraries.coreKtx)
    implementation("androidx.room:room-runtime:${libraries.versions.room.get()}")
    implementation("androidx.room:room-paging:${libraries.versions.room.get()}")
    ksp("androidx.room:room-compiler:${libraries.versions.room.get()}")

    testImplementation("junit:junit:${LibraryVersion.junit}")
    testImplementation("org.robolectric:robolectric:${LibraryVersion.robolectric}")
    testImplementation("io.mockk:mockk:${LibraryVersion.mockk}")
}
