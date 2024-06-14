
import com.android.build.gradle.tasks.GenerateBuildConfig
import property.BuildTool
import property.LibraryVersion

/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
plugins {
    id("com.android.library")
    id("kotlin-android")
    id("jacoco.definition")
}
//TODO apply from: '../jacoco.gradle'

android {
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
            isMinifyEnabled = true
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
        unitTests.isIncludeAndroidResources = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = LibraryVersion.composeCompiler
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
    implementation(project(":data"))
    implementation(project(":lib"))
    implementation(project(":search"))
    implementation(project(":ui"))
    implementation(project(":markdown"))

    implementation("androidx.core:core-ktx:${LibraryVersion.ktx}")

    implementation("com.jakewharton.timber:timber:${LibraryVersion.timber}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${LibraryVersion.coroutines}")

    implementation("androidx.compose.material3:material3:${LibraryVersion.composeMaterial3}")
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.paging:paging-compose:${LibraryVersion.pagingCompose}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${LibraryVersion.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${LibraryVersion.lifecycle}")

    implementation("androidx.paging:paging-common:${LibraryVersion.paging}")
    implementation("androidx.paging:paging-common-ktx:${LibraryVersion.paging}")
    implementation("androidx.work:work-runtime:${LibraryVersion.workManager}")

    testImplementation("junit:junit:${LibraryVersion.junit}")
    testImplementation("io.mockk:mockk:${LibraryVersion.mockk}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${LibraryVersion.coroutinesTest}")
    testImplementation("androidx.test.ext:junit-ktx:1.1.3")
    testImplementation("androidx.work:work-testing:2.7.1")
}
