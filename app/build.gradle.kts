
import property.BuildTool
import property.LibraryVersion
import property.Version
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("org.jetbrains.kotlin.plugin.serialization") version(libraries.versions.kotlin.get())
    id("com.android.application")
    id("kotlin-android")
    id("com.github.triplet.play")
    id("com.cookpad.android.plugin.license-tools")
    id("jacoco.definition")
    alias(libraries.plugins.composeCompiler)
}

// TODO apply(from = "../jacoco.gradle.kts")
// TODO apply(from = "../detekt.gradle.kts")

android {
    namespace = "jp.toastkid.yobidashi"

    compileSdkVersion(BuildTool.compileSdk)
    buildToolsVersion(BuildTool.buildTools)

    defaultConfig {
        applicationId = "jp.toastkid.yobidashi"
        minSdkVersion(BuildTool.minSdk)
        targetSdkVersion(BuildTool.targetSdk)

        versionCode = Version.code
        versionName = Version.name
        vectorDrawables.useSupportLibrary = true
    }
    signingConfigs {
        create("release") {
            // Initialize a new Properties() object called keystoreProperties.
            val keystoreProperties = Properties()

            // Load your keystore.properties file into the keystoreProperties object.
            BufferedInputStream(FileInputStream(rootProject.file("keystore.properties"))).use {
                keystoreProperties.load(it)
            }

            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
            storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
            storePassword = keystoreProperties.getProperty("storePassword")
        }
    }
    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            applicationIdSuffix = ".d"
            manifestPlaceholders["app_name"] = "Xobidashi"
            versionNameSuffix = ".d"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            manifestPlaceholders["app_name"] = "@string/app_name"
            signingConfig = signingConfigs.getByName("release")
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
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
        unitTests.isIncludeAndroidResources = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = LibraryVersion.composeCompiler
    }
    lintOptions {
        isCheckReleaseBuilds = false
    }
}

tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
    }
}

play {
    serviceAccountCredentials.set(file("signing/Google Play Android Developer-cbf2176b721a.json"))
    track.set("alpha")
}

dependencies {
    implementation(project(":lib"))
    implementation(project(":lib:display_effect"))
    implementation(project(":data"))
    implementation(project(":article"))
    implementation(project(":search"))
    implementation(project(":todo"))
    implementation(project(":music"))
    implementation(project(":pdf"))
    implementation(project(":api"))
    implementation(project(":rss"))
    implementation(project(":image"))
    implementation(project(":loan"))
    implementation(project(":about"))
    implementation(project(":barcode:ui"))
    implementation(project(":ui"))
    implementation(project(":editor"))
    implementation(project(":number"))
    implementation(project(":converter"))
    implementation(project(":calendar"))
    implementation(project(":web"))
    implementation(project(":chat"))
    implementation(project(":world"))
    implementation(project(":setting"))

    implementation(libraries.workManager)

    // Compose dependencies.
    implementation("androidx.compose.material3:material3:${LibraryVersion.composeMaterial3}")
    implementation(libraries.activityCompose)
    implementation("androidx.navigation:navigation-compose:${LibraryVersion.navigationCompose}")
    implementation("androidx.navigation:navigation-runtime-ktx:${LibraryVersion.navigationCompose}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${LibraryVersion.lifecycle}")
    implementation(libraries.coilCompose)
    implementation("androidx.core:core-ktx:${LibraryVersion.ktx}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${LibraryVersion.coroutines}")
    implementation("com.jakewharton.timber:timber:${LibraryVersion.timber}")
    implementation(libraries.kotlinSerialization)

    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.2")

    testImplementation("junit:junit:${LibraryVersion.junit}")
    testImplementation("org.robolectric:robolectric:${LibraryVersion.robolectric}")
    testImplementation("io.mockk:mockk:${LibraryVersion.mockk}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${LibraryVersion.coroutinesTest}")
}

configurations.implementation {
    exclude(group = "androidx.appcompat", module = "appcompat")
    exclude(group = "org.jetbrains.compose.runtime", module = "runtime")
    exclude(group = "org.jetbrains.compose.foundation", module = "foundation")
    exclude(group = "org.jetbrains.compose.material", module = "material")
}

repositories{
    flatDir{
        dirs("libs")
    }
}
