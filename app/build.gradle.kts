
import property.BuildTool
import property.LibraryVersion
import property.Version
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("org.jetbrains.kotlin.plugin.serialization") version("1.6.21")
    id("com.android.application")
    id("kotlin-android")
    id("org.jetbrains.kotlin.kapt")
    id("com.github.triplet.play")
    id("com.cookpad.android.plugin.license-tools")
    id("jacoco.definition")
}

// TODO apply(from = "../jacoco.gradle.kts")
// TODO apply(from = "../detekt.gradle.kts")

// Create a variable called keystorePropertiesFile, and initialize it to your
// keystore.properties file, in the rootProject folder.
val keystorePropertiesFile = rootProject.file("keystore.properties")

// Initialize a new Properties() object called keystoreProperties.
val keystoreProperties = Properties()

// Load your keystore.properties file into the keystoreProperties object.
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

android {
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
        }
    }
    buildFeatures {
        compose = true
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
    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
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
    implementation(project(":barcode"))
    implementation(project(":ui"))
    implementation(project(":editor"))
    implementation(project(":number"))
    implementation(project(":converter"))

    implementation("androidx.exifinterface:exifinterface:${LibraryVersion.exifinterface}")
    implementation("androidx.work:work-runtime:2.7.1")
    implementation("androidx.webkit:webkit:1.6.0")

    // Compose dependencies.
    implementation("androidx.compose.material3:material3:${LibraryVersion.composeMaterial3}")
    implementation("androidx.compose.runtime:runtime-livedata:${LibraryVersion.compose}")
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${LibraryVersion.lifecycle}")
    implementation("com.google.accompanist:accompanist-navigation-animation:0.23.1")
    implementation("com.google.accompanist:accompanist-flowlayout:0.24.5-alpha")
    implementation("io.coil-kt:coil-compose:${LibraryVersion.coilCompose}")
    implementation("com.godaddy.android.colorpicker:compose-color-picker:0.4.2")

    implementation("androidx.room:room-runtime:${LibraryVersion.room}")
    kapt("androidx.room:room-compiler:${LibraryVersion.room}")

    implementation("androidx.core:core-ktx:${LibraryVersion.ktx}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$BuildTool.kotlinVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${LibraryVersion.coroutines}")
    implementation("com.jakewharton.timber:timber:${LibraryVersion.timber}")
    implementation("com.squareup.okhttp3:okhttp:${LibraryVersion.okhttp}")
    implementation("com.squareup.retrofit2:retrofit:2.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
    implementation("org.jsoup:jsoup:${LibraryVersion.jsoup}")

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

kapt {
    javacOptions {
        option("-Xmaxerrs", 5000)
    }
}
