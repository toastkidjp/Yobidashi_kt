package jp.toastkid.navigation

import kotlinx.serialization.Serializable

@kotlinx.serialization.Serializable
sealed interface Screen {

    @kotlinx.serialization.Serializable
    object Empty : Screen

    @kotlinx.serialization.Serializable
    object Home : Screen

    @kotlinx.serialization.Serializable
    object Calendar : Screen {
        override val isTab: Boolean = true
    }

    @kotlinx.serialization.Serializable
    object Editor : Screen {
        override val isTab: Boolean = true
    }

    @kotlinx.serialization.Serializable
    data class Pdf(val id: String) : Screen {
        override val isTab: Boolean = true
    }

    @kotlinx.serialization.Serializable
    object WebBookmark : Screen

    @kotlinx.serialization.Serializable
    object WebHistory : Screen

    @kotlinx.serialization.Serializable
    object WebArchive : Screen

    @kotlinx.serialization.Serializable
    object BarcodeReader : Screen

    @kotlinx.serialization.Serializable
    object ImageViewer : Screen

    @kotlinx.serialization.Serializable
    object RssReader : Screen

    @kotlinx.serialization.Serializable
    object NumberPlace : Screen

    @kotlinx.serialization.Serializable
    object TaskBoard : Screen

    @kotlinx.serialization.Serializable
    object TaskList : Screen

    @kotlinx.serialization.Serializable
    object LoanCalculator : Screen

    @kotlinx.serialization.Serializable
    object AboutThisApp : Screen

    @kotlinx.serialization.Serializable
    object ConverterTool : Screen

    @kotlinx.serialization.Serializable
    object Chat : Screen

    @kotlinx.serialization.Serializable
    object Sensor : Screen

    @kotlinx.serialization.Serializable
    object WorldTime : Screen

    @kotlinx.serialization.Serializable
    object ArticleList : Screen {
        override val isTab: Boolean = true
    }

    @kotlinx.serialization.Serializable
    data class Article(val title: String) : Screen {
        override val isTab: Boolean = true
    }

    @kotlinx.serialization.Serializable
    data class Web(val url: String, val current: Boolean) : Screen {
        override val isTab: Boolean = true
    }

    @kotlinx.serialization.Serializable
    data class Search(
        val query: String,
        val title: String,
        val url: String
    ) : Screen

    @Serializable
    object SearchTop : Screen

    @kotlinx.serialization.Serializable
    object SearchHistory : Screen

    @kotlinx.serialization.Serializable
    object FavoriteSearch : Screen

    @kotlinx.serialization.Serializable
    data class Settings(val index: Int) : Screen

    val isTab: Boolean get() = false

    companion object {

        fun fromDestination(destination: String): Screen {
            return when {
                destination == "tab/web/current" -> Web("", current = true)
                destination == "tab/pdf/current" -> Pdf("new")
                destination == "tab/article/list" -> ArticleList
                destination == "tab/calendar" -> Calendar
                destination == "tab/editor/current" -> Editor
                destination.startsWith("tab/article/content/") -> {
                    Article(destination.split("/").last())
                }
                else -> Empty
            }
        }

    }

}