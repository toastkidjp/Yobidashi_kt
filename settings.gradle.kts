pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libraries") {
            version("kotlin", "2.2.20")
            version("kotlinCompilerExtension", "1.5.15")
            version("room", "2.7.2")
            version("cameraX", "1.5.0")
            plugin("composeCompiler", "org.jetbrains.kotlin.plugin.compose").versionRef("kotlin")
            library("coreKtx", "androidx.core", "core-ktx").version("1.16.0")
            library("activityCompose", "androidx.activity", "activity-compose").version("1.9.0")
            library("kotlinSerialization", "org.jetbrains.kotlinx", "kotlinx-serialization-json").version("1.6.0")
            library("composeUi", "androidx.compose.ui", "ui").version("1.7.4")
            library("workManager", "androidx.work", "work-runtime").version("2.10.0")
            library("jsoup", "org.jsoup", "jsoup").version("1.18.2")
            library("pagingCompose", "androidx.paging", "paging-compose").version("3.3.4")
            library("coilCompose", "io.coil-kt.coil3", "coil-compose").version("3.0.4")
            library("coilGif", "io.coil-kt.coil3", "coil-gif").version("3.0.4")
            library("coilNetwork", "io.coil-kt.coil3", "coil-network-okhttp").version("3.3.0")
            library("timber", "com.jakewharton.timber", "timber").version("5.0.1")
            library("exifinterface", "androidx.exifinterface", "exifinterface").version("1.3.3")
            library("composeMaterial3", "androidx.compose.material3", "material3").version("1.3.0")
            library("lifecycleRuntimeKtx", "androidx.lifecycle", "lifecycle-runtime-ktx").version("2.8.5")
        }

        create("testLibraries") {
            library("bytebuddy", "net.bytebuddy", "byte-buddy").version("1.17.5")
        }
    }
}

include(":todo")
include(":search")
include(":lib")
include(":data")
include(":app")
include(":article")
include(":licenses")
include(":music")
include(":pdf")
include(":api")
include(":rss")
include(":image")
include(":loan")
include(":about")
include(":barcode:ui")
include(":barcode:library")
include(":ui")
include(":editor")
include(":number")
include(":lib:display_effect")
include(":converter")
include(":calendar")
include(":web")
include(":chat")
include(":markdown")
include(":world")
include(":setting")
