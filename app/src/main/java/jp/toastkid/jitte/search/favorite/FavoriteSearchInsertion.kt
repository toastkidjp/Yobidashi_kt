package jp.toastkid.jitte.search.favorite

import android.content.Context

import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jp.toastkid.jitte.R
import jp.toastkid.jitte.libs.Toaster
import jp.toastkid.jitte.libs.db.DbInitter

/**
 * @author toastkidjp
 */
class FavoriteSearchInsertion(
        private val context: Context,
        private val category: String,
        private val query: String
) {

    fun insert() {
        insertFavoriteSearch(makeFavoriteSearch(category, query))
    }

    private fun insertFavoriteSearch(favoriteSearch: FavoriteSearch) {
        Completable.create { e ->
            DbInitter.init(context).insertIntoFavoriteSearch(favoriteSearch)
            e.onComplete()
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Toaster.tShort(
                            context,
                            context.getString(R.string.format_message_done_adding_favorite_search, query))
                }
    }

    private fun makeFavoriteSearch(category: String, query: String): FavoriteSearch {
        val fs = FavoriteSearch()
        fs.category = category
        fs.query = query
        return fs
    }

}