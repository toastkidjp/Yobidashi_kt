package jp.toastkid.yobidashi.search.favorite

import android.content.Context
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
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
        private val query: String
) {

    /**
     * Invoke action.
     */
    fun invoke(): Job {
        return CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                val repository =
                        DatabaseFinder().invoke(context).favoriteSearchRepository()
                repository.insert(makeFavoriteSearch(category, query))
            }

            Toaster.tShort(
                    context,
                    context.getString(R.string.format_message_done_adding_favorite_search, query)
            )
        }
    }

    /**
     * Make object.
     *
     * @param c Category string
     * @param q Query
     */
    private fun makeFavoriteSearch(c: String, q: String): FavoriteSearch {
        return FavoriteSearch().apply {
            category = c
            query = q
        }
    }

}