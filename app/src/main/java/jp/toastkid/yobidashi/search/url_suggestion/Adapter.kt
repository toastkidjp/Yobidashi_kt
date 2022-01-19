package jp.toastkid.yobidashi.search.url_suggestion

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import jp.toastkid.lib.view.list.CommonItemCallback
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.UrlItem
import jp.toastkid.yobidashi.search.SearchFragmentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * URL suggestion module's adapter.
 *
 * @param layoutInflater [LayoutInflater]
 * @param removeAt Callback of removing
 * @param itemDeletionUseCase Use for deletion item from database
 *
 * @author toastkidjp
 */
class Adapter(
        private val layoutInflater: LayoutInflater,
        private val removeAt: (UrlItem) -> Unit,
        private val itemDeletionUseCase: ItemDeletionUseCase
): ListAdapter<UrlItem, ViewHolder>(
    CommonItemCallback.with({ a, b -> a.itemId() == b.itemId() }, { a, b -> a == b })
) {

    /**
     * Item list.
     */
    private val suggestions: MutableList<UrlItem> = mutableListOf()

    private var viewModel: SearchFragmentViewModel? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(
                layoutInflater, R.layout.item_view_history, parent, false
        ))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.itemView.isVisible = item != null
        if (item == null) {
            return
        }

        item.bind(holder)
        holder.setOnClick { viewModel?.search(item.urlString()) }
        holder.setOnLongClick {
            viewModel?.searchOnBackground(item.urlString())
            true
        }
        holder.setDelete { removeAt(item) }
        holder.hideButton()
    }

    /**
     * Return is not empty for controlling visibility.
     *
     * @return is not empty?
     */
    fun isNotEmpty(): Boolean = currentList.isNotEmpty()

    /**
     * Return item.
     *
     * @return item
     */
    fun get(index: Int): UrlItem? {
        if (index < 0 || currentList.size <= index) {
            return null
        }
        return currentList[index]
    }

    /**
     * Remove at index.
     *
     * @param index
     * @return disposable
     */
    fun removeAt(index: Int): Job {
        return remove(get(index), index)
    }

    fun remove(item: UrlItem?, passedIndex: Int = -1): Job {
        if (item == null) {
            return Job()
        }

        return CoroutineScope(Dispatchers.Main).launch {
            val index = if (passedIndex == -1) currentList.indexOf(item) else passedIndex
            val newItems = mutableListOf<UrlItem>()
            newItems.addAll(currentList)
            withContext(Dispatchers.IO) {
                itemDeletionUseCase(item)
                newItems.removeAt(index)
            }

            submitList(newItems)
        }
    }

    fun setViewModel(viewModel: SearchFragmentViewModel) {
        this.viewModel = viewModel
    }

}