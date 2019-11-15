package jp.toastkid.yobidashi.browser.bookmark

import android.content.Context
import androidx.core.net.toUri
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.libs.storage.FilesDir
import timber.log.Timber
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
                    "Slashdot" to "https://m.slashdot.org"
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
    operator fun invoke(context: Context, onComplete: () -> Unit = {}): Disposable {
        val favicons = FilesDir(context, "favicons")
        val bookmarkRepository = DatabaseFinder().invoke(context).bookmarkRepository()

        return Completable.fromAction {
            defaultBookmarks.forEach {
                val parent = it.key
                bookmarkRepository.add(
                        Bookmark().also {
                            it.title = parent
                            it.parent = Bookmarks.ROOT_FOLDER_NAME
                            it.folder = true
                        }
                )
                it.value.entries.forEach { entry ->
                    bookmarkRepository.add(
                            Bookmark().also {
                                it.title = entry.key
                                it.url = entry.value
                                it.favicon =
                                        favicons.assignNewFile("${entry.value.toUri().host}.png")
                                                .absolutePath
                                it.parent = parent
                                it.folder = false
                            }
                    )
                }
            }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onComplete() },
                        Timber::e
                )
    }
}