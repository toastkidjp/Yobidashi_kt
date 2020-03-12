package jp.toastkid.yobidashi.browser.bookmark

import android.content.Context
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import timber.log.Timber

/**
 * @author toastkidjp
 */
class BookmarkInsertion (
        private val context: Context,
        private val title: String,
        private val url: String = "",
        private val faviconPath: String = "",
        private val parent: String = "",
        private val folder: Boolean = false
) {

    fun insert(): Disposable {
        if (title.isEmpty()) {
            return Disposables.empty()
        }
        return insert(Bookmark.make(title, url, faviconPath, parent, folder))
    }

    private fun insert(bookmark: Bookmark): Disposable {
        return Completable.fromAction {
            DatabaseFinder().invoke(context).bookmarkRepository()
                    .add(bookmark)
        }
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {},
                        Timber::e
                )
    }

}