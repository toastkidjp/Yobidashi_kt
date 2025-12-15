
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
        minSdk = BuildTool.minSdk
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
            val propertyFile = rootProject.file("keystore.properties")
            if (propertyFile.exists()) {
                BufferedInputStream(FileInputStream(propertyFile)).use {
                    keystoreProperties.load(it)
                }
            }

            keyAlias = keystoreProperties.getProperty("keyAlias") ?: System.getenv("KEY_ALIAS")
            keyPassword = keystoreProperties.getProperty("keyPassword") ?: System.getenv("KEY_PASSWORD")
            val keystoreFile = File(System.getenv("STORE_FILE_PATH") ?: "keystore.jks")
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
            }
            storePassword = keystoreProperties.getProperty("storePassword") ?: System.getenv("KEYSTORE_PASSWORD")
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
    composeOptions {
        kotlinCompilerExtensionVersion = libraries.versions.kotlinCompilerExtension.get()
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
    }

    lint {
        checkReleaseBuilds = false
    }
}

tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
    }
}

play {
    val path = System.getenv("SA_FILE_PATH")
    if (path != null) {
        serviceAccountCredentials.set(file(path))
    }
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
    implementation(libraries.composeMaterial3)
    implementation(libraries.activityCompose)
    implementation("androidx.navigation:navigation-compose:2.9.2")
    implementation("androidx.navigation:navigation-runtime-ktx:2.9.2")
    implementation(libraries.lifecycleRuntimeKtx)
    implementation(libraries.coreKtx)
    implementation(libraries.coroutines)
    implementation(libraries.timber)
    implementation(libraries.kotlinSerialization)

    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")

    testImplementation(testLibraries.junit)
    testImplementation(testLibraries.robolectric)
    testImplementation(testLibraries.mockK)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${LibraryVersion.coroutinesTest}")
    testImplementation(testLibraries.bytebuddy)
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
