package jp.toastkid.yobidashi.search.favorite

import android.support.annotation.DrawableRes
import android.support.v7.widget.RecyclerView
import android.view.View
import jp.toastkid.yobidashi.databinding.ItemSearchHistoryBinding

/**
 * @author toastkidjp
 */
internal class ModuleViewHolder(private val binding: ItemSearchHistoryBinding)
    : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.searchHistoryBookmark.visibility = View.GONE
    }

    fun setText(text: String) {
        binding.searchHistoryText.text = text
    }

    fun setImageRes(@DrawableRes iconId: Int) {
        binding.searchHistoryImage.setImageResource(iconId)
    }

    fun setOnClickAdd(history: FavoriteSearch, onClickAdd: (FavoriteSearch) -> Unit) {
        binding.searchHistoryAdd.setOnClickListener ({ _ ->
            onClickAdd(history)
        })
    }

    fun switchDividerVisibility(visible: Boolean) {
        binding.divider.visibility = if (visible) { View.VISIBLE } else { View.GONE }
    }

    fun setAddIcon(@DrawableRes addIcon: Int) {
        binding.searchHistoryAdd.setImageResource(addIcon)
    }
}
