package jp.toastkid.yobidashi.search.suggestion

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
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
        private val queryPutter: (String) -> Unit,
        private val suggestionsCallback: (String) -> Unit,
        private val onLongClicked: (String) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    /**
     * Suggestion items.
     */
    private val suggestions: MutableList<String> = mutableListOf()

    /**
     * Clear suggestions.
     */
    fun clear() {
        suggestions.clear()
    }

    /**
     * Add item.
     *
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
    }

    /**
     * Add(+) clicked action.
     *
     * @param suggestion
     */
    @SuppressLint("SetTextI18n")
    private fun onAddClicked(suggestion: String) {
        queryPutter("$suggestion ")
    }

    /**
     * Item clicked action.
     *
     * @param suggest
     */
    private fun onItemClicked(suggest: String) {
        queryPutter(suggest)
        try {
            suggestionsCallback(suggest)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}