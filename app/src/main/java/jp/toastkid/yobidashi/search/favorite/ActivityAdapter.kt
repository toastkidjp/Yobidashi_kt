package jp.toastkid.yobidashi.search.favorite

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.search.BackgroundSearchAction
import jp.toastkid.yobidashi.search.SearchCategory
import timber.log.Timber

/**
 * Favorite Search activity's adapter.
 *
 * @author toastkidjp
 */
internal class ActivityAdapter(
        context: Context,
        private val repository: FavoriteSearchRepository,
        private val searchAction: (SearchCategory, String) -> Unit,
        private val toasterCallback: (Int) -> Unit
) : RecyclerView.Adapter<FavoriteSearchHolder>() {

    /**
     * Layout inflater.
     */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val items = mutableListOf<FavoriteSearch>()

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): FavoriteSearchHolder = FavoriteSearchHolder(
            DataBindingUtil.inflate(inflater, R.layout.item_favorite_search, parent, false)
    )

    override fun onBindViewHolder(holder: FavoriteSearchHolder, position: Int) {
        bindViews(holder, items.get(position))
        holder.switchDividerVisibility(position != (itemCount - 1))
    }

    override fun getItemCount(): Int = items.size

    fun refresh(): Disposable {
        return Maybe.fromCallable { repository.findAll() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            items.addAll(it)
                            notifyDataSetChanged()
                        },
                        Timber::e
                )
    }

    /**
     * Remove item at position.
     *
     * @param position
     */
    fun deleteAt(position: Int): Disposable {
        return delete(items.get(position))
    }

    private fun delete(favoriteSearch: FavoriteSearch): Disposable {
        return Completable.fromAction { repository.delete(favoriteSearch) }
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {},
                        Timber::e
                )
    }

    /**
     * Bind views.
     *
     * @param holder
     * @param favoriteSearch
     */
    private fun bindViews(holder: FavoriteSearchHolder, favoriteSearch: FavoriteSearch) {
        val category = SearchCategory.findByCategory(favoriteSearch.category as String)
        holder.setImageId(category.iconId)

        val query = favoriteSearch.query
        holder.setText(query!!)

        holder.setClickAction(View.OnClickListener { searchAction(category, query) })

        holder.setRemoveAction(View.OnClickListener {
            delete(favoriteSearch)
            toasterCallback(R.string.settings_color_delete)
        })

        holder.itemView.setOnLongClickListener({ v ->
            BackgroundSearchAction(v, favoriteSearch.category, favoriteSearch.query).invoke()
            true
        })
    }

    fun clear() {
        repository.deleteAll()
    }
}