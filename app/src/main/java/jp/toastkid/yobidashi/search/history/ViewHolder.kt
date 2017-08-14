package jp.toastkid.yobidashi.search.history

import android.support.annotation.DrawableRes
import android.support.v7.widget.RecyclerView

import io.reactivex.functions.Consumer
import jp.toastkid.yobidashi.databinding.ItemSearchSuggestionBinding

/**
 * @author toastkidjp
 */
internal class ViewHolder(private val binding: ItemSearchSuggestionBinding) : RecyclerView.ViewHolder(binding.root) {

    fun setText(text: String) {
        binding.searchSuggestText.text = text
    }

    fun setImageRes(@DrawableRes iconId: Int) {
        binding.searchSuggestImage.setImageResource(iconId)
    }

    fun setOnClickAdd(text: String, onClickAdd: Consumer<String>) {
        binding.searchSuggestAdd.setOnClickListener ({ _ ->
            try {
                onClickAdd.accept(text)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }
}
