package jp.toastkid.yobidashi.search.favorite

import android.content.Context
import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.ViewGroup

import com.github.gfx.android.orma.Relation
import com.github.gfx.android.orma.widget.OrmaRecyclerViewAdapter

import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.functions.SingleValueCallback
import jp.toastkid.yobidashi.search.SearchCategory

/**
 * @author toastkidjp
 */
internal class Adapter(
        context: Context,
        relation: Relation<FavoriteSearch, *>,
        private val searchAction: SearchCallback,
        private val toasterCallback: SingleValueCallback<Int>
) : OrmaRecyclerViewAdapter<FavoriteSearch, FavoriteSearchHolder>(context, relation) {

    internal interface SearchCallback {
        fun accept(category: SearchCategory, query: String)
    }

    private val inflater: LayoutInflater

    init {
        this.inflater = LayoutInflater.from(context)
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): FavoriteSearchHolder {
        return FavoriteSearchHolder(
                DataBindingUtil
                        .inflate<FavoriteSearchItemBinding>(inflater, R.layout.favorite_search_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: FavoriteSearchHolder, position: Int) {
        bindViews(holder, relation.get(position))
    }

    override fun getItemCount(): Int {
        return relation.count()
    }

    fun removeAt(position: Int) {
        removeItemAsMaybe(relation.get(position))
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

    private fun bindViews(holder: FavoriteSearchHolder, favoriteSearch: FavoriteSearch) {
        val category = SearchCategory.findByCategory(favoriteSearch.category)
        holder.setImageId(category.iconId)

        val query = favoriteSearch.query
        holder.setText(query!!)

        holder.setClickAction { v -> searchAction.accept(category, query) }

        holder.setRemoveAction { v ->
            removeItemAsMaybe(favoriteSearch).subscribeOn(Schedulers.io()).subscribe()
            toasterCallback.accept(R.string.settings_color_delete)
        }
    }
}