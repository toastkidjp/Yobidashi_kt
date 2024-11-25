pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libraries") {
            version("kotlin", "2.0.20")
            plugin("composeCompiler", "org.jetbrains.kotlin.plugin.compose").version("2.0.21")
            library("kotlinSerialization", "org.jetbrains.kotlinx", "kotlinx-serialization-json").version("1.6.0")
            library("composeUi", "androidx.compose.ui", "ui").version("1.7.4")
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
