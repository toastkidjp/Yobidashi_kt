package jp.toastkid.yobidashi.search.favorite

import android.content.Context
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DbInitializer

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
    fun invoke() {
        Completable
                .fromAction { insert() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Toaster.tShort(
                            context,
                            context.getString(R.string.format_message_done_adding_favorite_search, query))
                }
    }

    /**
     * Insert record.
     */
    private fun insert() {
        DbInitializer.init(context).insertIntoFavoriteSearch(makeFavoriteSearch(category, query))
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