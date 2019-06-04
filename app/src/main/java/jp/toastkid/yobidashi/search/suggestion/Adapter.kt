package jp.toastkid.yobidashi.search.suggestion

import android.annotation.SuppressLint
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import jp.toastkid.yobidashi.R
import timber.log.Timber

/**
 * Suggest list adapter.
 *
 * @param layoutInflater Layout inflater
 * @param searchInput EditText
 * @param suggestionsCallback Using selected suggest action
 *
 * @author toastkidjp
 */
internal class Adapter (
        private val layoutInflater: LayoutInflater,
        private val searchInput: EditText,
        private val suggestionsCallback: (String) -> Unit,
        private val onLongClicked: (String) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    /** Suggestion items.  */
    private val suggestions: MutableList<String> = mutableListOf()

    /**
     * Clear suggestions.
     */
    fun clear() {
        suggestions.clear()
        notifyDataSetChanged()
    }

    /**
     * Add item.
     * @param s
     */
    fun add(s: String) {
        suggestions.add(s)
    }

    override fun getItemCount(): Int {
        return suggestions.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(layoutInflater, R.layout.item_search_suggestion, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = suggestions[position]
        holder.setText(item)
        holder.itemView.setOnClickListener { onItemClicked(item) }
        holder.itemView.setOnLongClickListener {
            onLongClicked(item)
            true
        }
        holder.setOnClickAdd(View.OnClickListener{ onAddClicked(item) })
        holder.switchDividerVisibility(position != (itemCount - 1))
    }

    /**
     * Add(+) clicked action.
     * @param suggestion
     */
    @SuppressLint("SetTextI18n")
    private fun onAddClicked(suggestion: String) {
        searchInput.setText("$suggestion ")
        searchInput.setSelection(searchInput.text.toString().length)
    }

    /**
     * Item clicked action.
     * @param suggest
     */
    private fun onItemClicked(suggest: String) {
        searchInput.setText(suggest)
        searchInput.setSelection(suggest.length)
        try {
            suggestionsCallback(suggest)
        } catch (e: Exception) {
            Timber.e(e)
        }

    }
}