package jp.toastkid.jitte.browser.bookmark

import android.content.Context
import com.github.gfx.android.orma.annotation.OnConflict
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import jp.toastkid.jitte.browser.bookmark.model.Bookmark
import jp.toastkid.jitte.libs.db.DbInitter
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
        return insert(makeItem(title, url, faviconPath, parent, folder))
    }

    private fun insert(bookmark: Bookmark): Disposable {
        return Completable.create { e ->
            DbInitter.init(context)
                    .relationOfBookmark()
                    .inserter(OnConflict.REPLACE)
                    .execute(bookmark)
            e.onComplete()
        }.subscribeOn(Schedulers.io()).subscribe({}, {Timber.e(it)})
    }

    private fun makeItem(
            title: String,
            url: String = "",
            faviconPath: String = "",
            parent: String = "parent",
            folder: Boolean = false
    ): Bookmark {
        val bookmark = Bookmark()
        bookmark.title = title
        bookmark.url = url
        bookmark.favicon = faviconPath
        bookmark.last_viewed = System.currentTimeMillis()
        bookmark.parent = parent
        bookmark.folder = folder
        return bookmark
    }

}