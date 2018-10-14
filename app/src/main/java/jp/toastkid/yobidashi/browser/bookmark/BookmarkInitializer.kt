package jp.toastkid.yobidashi.browser.bookmark

import android.content.Context
import androidx.core.net.toUri
import jp.toastkid.yobidashi.libs.storage.FilesDir
import java.util.*

/**
 * Bookmark initializer.
 *
 * @author toastkidjp
 */
object BookmarkInitializer {

    /**
     * Default bookmarks.
     */
    private val DEFAULT_BOOKMARKS: Map<String, Map<String, String>> = mapOf(
            "Recommended" to mapOf(
                    "Google Translate" to "https://translate.google.com/",
                    "YouTube" to "https://www.youtube.com/",
                    "AccuWeather" to "https://www.accuweather.com/",
                    "Wikipedia" to "https://${Locale.getDefault().language}.wikipedia.org/",
                    "Google Map" to "https://www.google.co.jp/maps/",
                    "Yelp" to "https://www.yelp.com/",
                    "Amazon" to "https://www.amazon.com/",
                    "Project Gutenberg" to "http://www.gutenberg.org/",
                    "Expedia" to "https://www.expedia.com"
                    ),
            "Search" to mapOf(
                    "Google" to "https://www.google.com/",
                    "Bing" to "https://www.bing.com/",
                    "Yandex" to "https://yandex.com/",

                    "Yahoo!" to "https://www.yahoo.com/",
                    "Yahoo! JAPAN" to "https://www.yahoo.co.jp/"
            ),
            "SNS" to mapOf(
                    "Instagram" to "https://www.instagram.com/",
                    "Twitter" to "https://twitter.com/",
                    "Facebook" to "https://www.facebook.com/"
            )
    )

    /**
     * Invoke action.
     *
     * @param context
     */
    operator fun invoke(context: Context) {
        val favicons = FilesDir(context, "favicons")

        DEFAULT_BOOKMARKS.forEach {
            val parent = it.key
            BookmarkInsertion(
                    context = context,
                    title = parent,
                    parent = Bookmarks.ROOT_FOLDER_NAME,
                    folder = true
            ).insert()
            it.value.entries.forEach {
                BookmarkInsertion(
                        context,
                        it.key,
                        it.value,
                        favicons.assignNewFile("${it.value.toUri().host}.png").absolutePath,
                        parent
                ).insert()
            }
        }
    }
}