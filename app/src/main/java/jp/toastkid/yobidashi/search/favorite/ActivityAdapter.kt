package jp.toastkid.yobidashi.search.favorite

import android.content.Context
import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.gfx.android.orma.Relation
import com.github.gfx.android.orma.widget.OrmaRecyclerViewAdapter
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.search.BackgroundSeachAction
import jp.toastkid.yobidashi.search.SearchCategory

/**
 * Favorite Search activity's adapter.
 *
 * @author toastkidjp
 */
internal class ActivityAdapter(
        context: Context,
        relation: Relation<FavoriteSearch, *>,
        private val searchAction: (SearchCategory, String) -> Unit,
        private val toasterCallback: (Int) -> Unit
) : OrmaRecyclerViewAdapter<FavoriteSearch, FavoriteSearchHolder>(context, relation) {

    /**
     * Layout inflater.
     */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): FavoriteSearchHolder = FavoriteSearchHolder(
            DataBindingUtil.inflate(inflater, R.layout.item_favorite_search, parent, false)
    )

    override fun onBindViewHolder(holder: FavoriteSearchHolder, position: Int) {
        bindViews(holder, relation.get(position))
        holder.switchDividerVisibility(position != (itemCount - 1))
    }

    override fun getItemCount(): Int = relation.count()

    /**
     * Remove item at position.
     *
     * @param position
     */
    fun removeAt(position: Int) {
        removeItemAsMaybe(relation.get(position))
                .subscribeOn(Schedulers.io())
                .subscribe()
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
            removeItemAsMaybe(favoriteSearch).subscribeOn(Schedulers.io()).subscribe()
            toasterCallback(R.string.settings_color_delete)
        })

        holder.itemView.setOnLongClickListener({ v ->
            BackgroundSeachAction(v, favoriteSearch.category, favoriteSearch.query).invoke()
            true
        })
    }
}