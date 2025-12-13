
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
    alias(libraries.plugins.composeCompiler)
}
//TODO apply from: '../jacoco.gradle'

android {
    namespace = "jp.toastkid.article_viewer"

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

    implementation(libraries.coreKtx)

    implementation(libraries.timber)
    implementation(libraries.coroutines)

    implementation(libraries.composeMaterial3)
    implementation(libraries.activityCompose)
    implementation(libraries.pagingCompose)
    implementation(libraries.lifecycleRuntimeKtx)
    implementation(libraries.lifecycleRuntimeCompose)
    implementation(libraries.lifecycleViewModelCompose)

    implementation(libraries.workManager)

    testImplementation(testLibraries.junit)
    testImplementation("io.mockk:mockk:${LibraryVersion.mockk}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${LibraryVersion.coroutinesTest}")
    testImplementation("androidx.test.ext:junit-ktx:1.1.3")
    testImplementation("androidx.work:work-testing:2.7.1")
}
