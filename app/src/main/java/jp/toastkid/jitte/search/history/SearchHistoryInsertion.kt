package jp.toastkid.jitte.search.history

import android.content.Context

import com.github.gfx.android.orma.annotation.OnConflict

import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.jitte.libs.db.DbInitter

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
            DbInitter.init(context)
                    .relationOfSearchHistory()
                    .inserter(OnConflict.REPLACE)
                    .execute(searchHistory)
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