package jp.toastkid.search.favorite

import android.content.Context
import jp.toastkid.data.repository.factory.RepositoryFactory
import jp.toastkid.yobidashi.search.favorite.FavoriteSearch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class FavoriteSearchInsertion(
        private val context: Context,
        private val category: String,
        private val query: String,
        private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
        private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Invoke action.
     */
    fun invoke(): Job {
        return CoroutineScope(mainDispatcher).launch {
            withContext(ioDispatcher) {
                val repository =
                    RepositoryFactory().favoriteSearchRepository(context)
                repository.insert(FavoriteSearch.with(category, query))
            }
        }
    }

}