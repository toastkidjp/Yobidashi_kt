package jp.toastkid.yobidashi.search.suggestion

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

import java.util.ArrayList

import io.reactivex.functions.Consumer
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemSearchSuggestionBinding

/**
 * Suggest list adapter.

 * @author toastkidjp
 */
internal class Adapter
/**

 * @param inflater
 * *
 * @param input
 * *
 * @param suggestConsumer
 */
(
        /** Layout inflater.  */
        private val mInflater: LayoutInflater,
        /** EditText.  */
        private val mSearchInput: EditText,
        /** Using selected suggest action.  */
        private val mSuggestConsumer: Consumer<String>
) : RecyclerView.Adapter<ViewHolder>() {

    /** Suggestion items.  */
    private val mSuggestions: MutableList<String>

    init {
        mSuggestions = ArrayList<String>(10)
    }

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
                DataBindingUtil.inflate<ItemSearchSuggestionBinding>(mInflater, R.layout.item_search_suggestion, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mSuggestions[position]
        holder.setText(item)
        holder.itemView.setOnClickListener { v -> onItemClicked(item) }
        holder.setOnClickAdd(View.OnClickListener{ v -> onAddClicked(item) })
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
            mSuggestConsumer.accept(suggest)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}