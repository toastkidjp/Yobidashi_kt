package jp.toastkid.yobidashi.search.favorite

import android.content.Context
import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.github.gfx.android.orma.Relation
import com.github.gfx.android.orma.widget.OrmaRecyclerViewAdapter
import io.reactivex.functions.BiConsumer
import io.reactivex.functions.Consumer

import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FavoriteSearchItemBinding
import jp.toastkid.yobidashi.libs.functions.SingleValueCallback
import jp.toastkid.yobidashi.search.SearchCategory

/**
 * @author toastkidjp
 */
internal class Adapter(
        context: Context,
        relation: Relation<FavoriteSearch, *>,
        private val searchAction: BiConsumer<SearchCategory, String>,
        private val toasterCallback: Consumer<Int>
) : OrmaRecyclerViewAdapter<FavoriteSearch, FavoriteSearchHolder>(context, relation) {

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
        val category = SearchCategory.findByCategory(favoriteSearch.category as String)
        holder.setImageId(category.iconId)

        val query = favoriteSearch.query
        holder.setText(query!!)

        holder.setClickAction(View.OnClickListener { searchAction.accept(category, query) })

        holder.setRemoveAction(View.OnClickListener {
            removeItemAsMaybe(favoriteSearch).subscribeOn(Schedulers.io()).subscribe()
            toasterCallback.accept(R.string.settings_color_delete)
        })
    }
}