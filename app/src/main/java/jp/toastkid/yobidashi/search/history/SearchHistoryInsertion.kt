package jp.toastkid.yobidashi.search.history

import android.content.Context
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import timber.log.Timber

/**
 *
 * @param context [Context]
 * @param category search category name
 * @param query search query
 *
 * @author toastkidjp
 */
class SearchHistoryInsertion private constructor(
        context: Context,
        private val category: String,
        private val query: String
) {

    private val repository =
            DatabaseFinder().invoke(context).searchHistoryRepository()

    fun insert(): Disposable {
        if (category.isEmpty() || query.isEmpty()) {
            return Disposables.empty()
        }
        return insert(SearchHistory.make(category, query))
    }

    private fun insert(searchHistory: SearchHistory) =
            Completable.fromAction { repository.insert(searchHistory) }
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            {},
                            Timber::e
                    )

    companion object {

        /**
         * Make search history insertion object.
         *
         * @param context [Context]
         * @param category search category name
         * @param query search query
         */
        fun make(context: Context, category: String, query: String) =
                SearchHistoryInsertion(context, category, query)
    }
}