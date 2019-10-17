package jp.toastkid.yobidashi.search.favorite

import android.content.Context
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import timber.log.Timber

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
    fun invoke(): Disposable {
        return Completable
                .fromAction { insert() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            Toaster.tShort(
                                    context,
                                    context.getString(R.string.format_message_done_adding_favorite_search, query))
                        },
                        Timber::e
                )
    }

    /**
     * Insert record.
     */
    private fun insert() {
        val repository = DatabaseFinder().invoke(context).favoriteSearchRepository()
        repository.insert(makeFavoriteSearch(category, query))
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