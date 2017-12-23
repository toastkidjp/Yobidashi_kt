package jp.toastkid.yobidashi.browser.bookmark

import android.content.Context
import android.net.Uri
import jp.toastkid.yobidashi.libs.storage.FilesDir
import java.util.*

/**
 * @author toastkidjp
 */
object BookmarkInitializer {

    private val DEFAULT_BOOKMARKS: Map<String, String> = mapOf(
            "Google Translate" to "https://translate.google.com/",
            "Twitter" to "https://twitter.com/",
            "Google" to "https://www.google.com/",
            "YouTube" to "https://www.youtube.com/",
            "Bing" to "https://www.bing.com/",
            "Yandex" to "https://yandex.com/",
            "Wikipedia" to "https://${Locale.getDefault().language}.wikipedia.org/",
            "Instagram" to "https://www.instagram.com/",
            "Google Map" to "https://www.google.co.jp/maps/",
            "Yelp" to "https://www.yelp.com/",
            "Amazon" to "https://www.amazon.com/",
            "Facebook" to "https://www.facebook.com/",
            "Project Gutenberg" to "http://www.gutenberg.org/",
            "Yahoo!" to "https://www.yahoo.com/",
            "Yahoo! JAPAN" to "https://www.yahoo.co.jp/"
            )

    fun invoke(context: Context) {
        val favicons = FilesDir(context, "favicons")

        DEFAULT_BOOKMARKS.entries.forEach {
            BookmarkInsertion(
                    context,
                    it.key,
                    it.value,
                    favicons.assignNewFile(Uri.parse(it.value).host + ".png").absolutePath,
                    Bookmarks.ROOT_FOLDER_NAME
            ).insert()
        }
    }
}