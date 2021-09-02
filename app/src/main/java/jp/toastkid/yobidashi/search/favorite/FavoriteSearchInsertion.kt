package jp.toastkid.yobidashi.search.favorite

import android.content.Context
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
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
                        DatabaseFinder().invoke(context).favoriteSearchRepository()
                repository.insert(FavoriteSearch.with(category, query))
            }

            Toaster.tShort(
                    context,
                    context.getString(R.string.format_message_done_adding_favorite_search, query)
            )
        }
    }

}