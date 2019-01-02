package jp.toastkid.yobidashi.browser.history

import android.content.Context
import com.github.gfx.android.orma.annotation.OnConflict
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.libs.db.DbInitializer

/**
 * @author toastkidjp
 */
class ViewHistoryInsertion private constructor(
        private val context: Context,
        private val title: String,
        private val url: String,
        private val faviconPath: String
) {

    fun insert(): Disposable {
        if (title.isEmpty() || url.isEmpty()) {
            return Disposables.empty()
        }
        return insert(makeItem(title, url, faviconPath))
    }

    private fun insert(searchHistory: ViewHistory): Disposable {
        return Completable.create { e ->
            DbInitializer.init(context)
                    .relationOfViewHistory()
                    .inserter(OnConflict.REPLACE)
                    .execute(searchHistory)
            e.onComplete()
        }.subscribeOn(Schedulers.io()).subscribe()
    }

    private fun makeItem(title: String, url: String, faviconPath: String): ViewHistory {
        val sh = ViewHistory()
        sh.title = title
        sh.url = url
        sh.favicon = faviconPath
        sh.last_viewed = System.currentTimeMillis()
        return sh
    }

    companion object {

        fun make(
                context: Context,
                title: String,
                url: String,
                faviconPath: String
        ): ViewHistoryInsertion {
            return ViewHistoryInsertion(context, title, url, faviconPath)
        }
    }
}