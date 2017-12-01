package jp.toastkid.yobidashi.search.suggestion

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemSearchSuggestionBinding
import timber.log.Timber

/**
 * Suggest list adapter.
 *
 * @param mInflater Layout inflater
 * @param mSearchInput EditText
 * @param mSuggestionsCallback Using selected suggest action
 *
 * @author toastkidjp
 */
internal class Adapter (
        private val mInflater: LayoutInflater,
        private val mSearchInput: EditText,
        private val mSuggestionsCallback: (String) -> Unit,
        private val onLongClicked: (String) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    /** Suggestion items.  */
    private val mSuggestions: MutableList<String> = mutableListOf()

    /**
     * Clear suggestions.
     */
    fun clear() {
        mSuggestions.clear()
        notifyDataSetChanged()
    }

    /**
     * Add item.
     * @param s
     */
    fun add(s: String) {
        mSuggestions.add(s)
    }

    override fun getItemCount(): Int {
        return mSuggestions.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate<ItemSearchSuggestionBinding>(
                        mInflater, R.layout.item_search_suggestion, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mSuggestions[position]
        holder.setText(item)
        holder.itemView.setOnClickListener { v -> onItemClicked(item) }
        holder.itemView.setOnLongClickListener {
            onLongClicked(item)
            true
        }
        holder.setOnClickAdd(View.OnClickListener{ v -> onAddClicked(item) })
        holder.switchDividerVisibility(position != (itemCount - 1))
    }

    /**
     * Add(+) clicked action.
     * @param suggestion
     */
    private fun onAddClicked(suggestion: String) {
        mSearchInput.setText(suggestion + " ")
        mSearchInput.setSelection(mSearchInput.text.toString().length)
    }

    /**
     * Item clicked action.
     * @param suggest
     */
    private fun onItemClicked(suggest: String) {
        mSearchInput.setText(suggest)
        mSearchInput.setSelection(suggest.length)
        try {
            mSuggestionsCallback(suggest)
        } catch (e: Exception) {
            Timber.e(e)
        }

    }
}