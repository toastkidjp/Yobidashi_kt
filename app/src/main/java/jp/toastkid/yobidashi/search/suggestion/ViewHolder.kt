package jp.toastkid.yobidashi.search.suggestion

import android.support.v7.widget.RecyclerView
import android.view.View

import jp.toastkid.yobidashi.databinding.ItemSearchSuggestionBinding

/**
 * Search suggestion item's view holder.

 * @author toastkidjp
 */
internal class ViewHolder
/**
 * Initialize with data binding object.
 * @param binding
 */
(
        /** Data binding object.  */
        private val binding: ItemSearchSuggestionBinding) : RecyclerView.ViewHolder(binding.root) {

    /**
     * Set suggestion's text.
     * @param text
     */
    fun setText(text: String) {
        binding.searchSuggestText.text = text
    }

    /**
     * Set add button's behavior when it click.
     * @param listener
     */
    fun setOnClickAdd(listener: View.OnClickListener) {
        binding.searchSuggestAdd.setOnClickListener(listener)
    }
}
