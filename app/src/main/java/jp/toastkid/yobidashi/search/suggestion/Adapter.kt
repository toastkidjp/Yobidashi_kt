package jp.toastkid.yobidashi.search.suggestion

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import jp.toastkid.lib.view.list.CommonItemCallback
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.search.SearchFragmentViewModel

/**
 * Suggest list adapter.
 *
 * @param layoutInflater Layout inflater
 *
 * @author toastkidjp
 */
internal class Adapter (
        private val layoutInflater: LayoutInflater
) : ListAdapter<String, ViewHolder>(
    CommonItemCallback.with<String>({ a, b -> a.hashCode() == b.hashCode() }, { a, b -> a == b })
) {

    private var viewModel: SearchFragmentViewModel? = null

    /**
     * Clear suggestions.
     */
    fun clear() {
        submitList(emptyList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(layoutInflater, ITEM_LAYOUT_ID, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.setText(item)
        holder.itemView.setOnClickListener {
            viewModel?.putQuery(item)
            viewModel?.search(item)
        }
        holder.itemView.setOnLongClickListener {
            viewModel?.searchOnBackground(item)
            true
        }
        holder.setOnClickAdd { viewModel?.putQuery("$item ") }
    }

    fun setViewModel(viewModel: SearchFragmentViewModel) {
        this.viewModel = viewModel
    }

    fun replace(suggestions: List<String>) {
        submitList(suggestions)
    }

    companion object {

        @LayoutRes
        private const val ITEM_LAYOUT_ID = R.layout.item_search_suggestion

    }
}