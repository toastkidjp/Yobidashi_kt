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
}

//TODO apply(from = "../jacoco.gradle")

android {
    namespace = "jp.toastkid.search"

    compileSdkVersion(BuildTool.compileSdk)

    defaultConfig {
        minSdkVersion(BuildTool.minSdk)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = LibraryVersion.composeCompiler
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
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
    implementation(project(":api"))
    implementation(project(":data"))
    implementation(project(":lib"))
    implementation(project(":ui"))

    implementation("androidx.compose.material3:material3:${LibraryVersion.composeMaterial3}")
    implementation("androidx.activity:activity-compose:${LibraryVersion.activityCompose}")
    implementation("io.coil-kt:coil-compose:${LibraryVersion.coilCompose}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${LibraryVersion.lifecycle}")
    implementation("com.jakewharton.timber:timber:${LibraryVersion.timber}")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:${BuildTool.kotlinVersion}")
    implementation("androidx.core:core-ktx:${LibraryVersion.ktx}")
    testImplementation("junit:junit:${LibraryVersion.junit}")
    testImplementation("org.robolectric:robolectric:${LibraryVersion.robolectric}")
    testImplementation("io.mockk:mockk:${LibraryVersion.mockk}")
}
