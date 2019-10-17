package jp.toastkid.yobidashi.search.history

import android.content.Context
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.libs.db.DatabaseFinder

/**
 * @author toastkidjp
 */
class SearchHistoryInsertion private constructor(
        private val context: Context,
        private val category: String,
        private val query: String
) {

    fun insert(): Disposable {
        if (category.isEmpty() || query.isEmpty()) {
            return EMPTY
        }
        return insert(makeItem(category, query))
    }

    private fun insert(searchHistory: SearchHistory): Disposable {
        return Completable.create { e ->
            val repository = DatabaseFinder().invoke(context).searchHistoryRepository()
            repository.insert(searchHistory)
            e.onComplete()
        }.subscribeOn(Schedulers.io()).subscribe()
    }

    private fun makeItem(category: String, query: String): SearchHistory {
        val sh = SearchHistory()
        sh.key = category + query
        sh.category = category
        sh.query = query
        sh.timestamp = System.currentTimeMillis()
        return sh
    }

    companion object {

        private val EMPTY = object : Disposable {
            override fun dispose() {

            }

            override fun isDisposed(): Boolean {
                return false
            }
        }

        fun make(
                context: Context,
                category: String,
                query: String
        ): SearchHistoryInsertion {
            return SearchHistoryInsertion(context, category, query)
        }
    }
}