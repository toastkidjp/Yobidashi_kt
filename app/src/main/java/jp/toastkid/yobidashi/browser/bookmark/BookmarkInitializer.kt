package jp.toastkid.yobidashi.browser.bookmark

import android.content.Context
import androidx.core.net.toUri
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.libs.storage.FilesDir
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Bookmark initializer.
 *
 * @author toastkidjp
 */
class BookmarkInitializer {

    /**
     * Default bookmarks.
     */
    private val defaultBookmarks: Map<String, Map<String, String>> = mapOf(
            "Recommended" to mapOf(
                    "Google Translate" to "https://translate.google.com/",
                    "YouTube" to "https://www.youtube.com/",
                    "AccuWeather" to "https://www.accuweather.com/",
                    "Wikipedia" to "https://${Locale.getDefault().language}.wikipedia.org/",
                    "Google Map" to "https://www.google.co.jp/maps/",
                    "Yelp" to "https://www.yelp.com/",
                    "Amazon" to "https://www.amazon.com/",
                    "Project Gutenberg" to "http://www.gutenberg.org/",
                    "Expedia" to "https://www.expedia.com",
                    "Slashdot" to "https://m.slashdot.org",
                    "Financial Times" to "https://www.ft.com/"
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
    operator fun invoke(context: Context, onComplete: () -> Unit = {}): Job {
        val favicons = FilesDir(context, "favicons")
        val bookmarkRepository = DatabaseFinder().invoke(context).bookmarkRepository()

        // TODO Extract to function.
        return CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                defaultBookmarks.forEach {
                    val parent = it.key

                    bookmarkRepository.add(makeFolder(parent))

                    it.value.entries.forEach { entry ->
                        bookmarkRepository.add(makeItem(entry, favicons, parent))
                    }
                }
            }

            onComplete()
        }
    }

    private fun makeItem(entry: Map.Entry<String, String>, favicons: FilesDir, parent: String) =
            Bookmark().also {
                it.title = entry.key
                it.url = entry.value
                it.favicon =
                        favicons.assignNewFile("${entry.value.toUri().host}.png")
                                .absolutePath
                it.parent = parent
                it.folder = false
            }

    private fun makeFolder(parent: String) =
            Bookmark().also {
                it.title = parent
                it.parent = Bookmark.getRootFolderName()
                it.folder = true
            }
}