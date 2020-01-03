package jp.toastkid.yobidashi.browser.history

import android.content.Context
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import timber.log.Timber

/**
 * TODO clean up code.
 *
 * @author toastkidjp
 */
class ViewHistoryInsertion private constructor(
        private val context: Context,
        private val title: String,
        private val url: String,
        private val faviconPath: String
) {

    operator fun invoke(): Disposable =
            if (title.isEmpty() || url.isEmpty()) Disposables.disposed()
            else insert(makeItem(title, url, faviconPath))

    private fun insert(searchHistory: ViewHistory): Disposable {
        return Completable.fromAction {
            DatabaseFinder().invoke(context)
                    .viewHistoryRepository()
                    .add(searchHistory)
        }
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {},
                        Timber::e
                )
    }

    private fun makeItem(title: String, url: String, faviconPath: String): ViewHistory {
        val sh = ViewHistory()
        sh.title = title
        sh.url = url
        sh.favicon = faviconPath
        sh.lastViewed = System.currentTimeMillis()
        return sh
    }

    companion object {

        fun make(
                context: Context,
                title: String,
                url: String,
                faviconPath: String
        ) = ViewHistoryInsertion(context, title, url, faviconPath)
    }
}