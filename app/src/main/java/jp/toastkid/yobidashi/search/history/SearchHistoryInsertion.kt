package jp.toastkid.yobidashi.search.history

import android.content.Context
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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
        private val query: String,
        private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val repository =
            DatabaseFinder().invoke(context).searchHistoryRepository()

    fun insert(): Job {
        if (category.isEmpty() || query.isEmpty()) {
            return Job()
        }

        // TODO Fix return type.
        return CoroutineScope(dispatcher).launch {
            repository.insert(SearchHistory.make(category, query))
        }
    }

    companion object {

        /**
         * Make search history insertion object.
         *
         * @param context [Context]
         * @param category search category name
         * @param query search query
         */
        fun make(context: Context, category: String, query: String, dispatcher: CoroutineDispatcher = Dispatchers.IO) =
                SearchHistoryInsertion(context, category, query, dispatcher)
    }
}