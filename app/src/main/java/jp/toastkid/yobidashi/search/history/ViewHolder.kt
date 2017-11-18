package jp.toastkid.yobidashi.search.history

import android.support.annotation.DrawableRes
import android.support.v7.widget.RecyclerView
import android.view.View
import jp.toastkid.yobidashi.databinding.ItemSearchHistoryBinding
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchInsertion

/**
 * @author toastkidjp
 */
internal class ViewHolder(private val binding: ItemSearchHistoryBinding)
    : RecyclerView.ViewHolder(binding.root) {

    fun setText(text: String) {
        binding.searchHistoryText.text = text
    }

    fun setImageRes(@DrawableRes iconId: Int) {
        binding.searchHistoryImage.setImageResource(iconId)
    }

    fun setOnClickAdd(history: SearchHistory, onClickAdd: (SearchHistory) -> Unit) {
        binding.searchHistoryAdd.setOnClickListener ({ _ ->
            onClickAdd(history)
        })
    }

    fun switchDividerVisibility(visible: Boolean) {
        binding.divider.visibility = if (visible) { View.VISIBLE } else { View.GONE }
    }

    fun setFavorite(category: String, query: String) {
        binding.searchHistoryBookmark.setOnClickListener { v ->
            FavoriteSearchInsertion(v.context, category, query).invoke()
        }
    }

    fun setAddIcon(@DrawableRes addIcon: Int) {
        binding.searchHistoryAdd.setImageResource(addIcon)
    }
}
