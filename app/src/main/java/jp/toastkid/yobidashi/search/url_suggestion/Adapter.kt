package jp.toastkid.yobidashi.search.url_suggestion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.UrlItem
import jp.toastkid.yobidashi.browser.history.ViewHistoryRepository
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
 * @param browseCallback
 * @param browseBackgroundCallback
 * @author toastkidjp
 */
class Adapter(
        private val layoutInflater: LayoutInflater,
        private val removeAt: (UrlItem) -> Unit,
        private val browseCallback: (String) -> Unit,
        private val browseBackgroundCallback: (String) -> Unit
): RecyclerView.Adapter<ViewHolder>() {

    /**
     * Item list.
     */
    private val suggestions: MutableList<UrlItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(
                layoutInflater, R.layout.item_view_history, parent, false
        ))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = suggestions.get(position)
        item.bind(holder)
        holder.setOnClick(View.OnClickListener { browseCallback(item.urlString()) })
        holder.setOnLongClick(View.OnLongClickListener {
            browseBackgroundCallback(item.urlString())
            true
        })
        holder.setDelete(View.OnClickListener { removeAt(item) })
    }

    override fun getItemCount(): Int = suggestions.size

    /**
     * Add item to list.
     *
     * @param item
     */
    fun add(item: UrlItem?) {
        item?.let { suggestions.add(it) }
    }

    /**
     * Clear items.
     */
    fun clear() {
        suggestions.clear()
    }

    /**
     * Return is not empty for controlling visibility.
     *
     * @return is not empty?
     */
    fun isNotEmpty(): Boolean = suggestions.isNotEmpty()

    /**
     * Return item.
     *
     * @return item
     */
    fun get(index: Int): UrlItem = suggestions.get(index)

    /**
     * Remove at index.
     *
     * @param viewHistoryRepository
     * @param index
     * @return disposable
     */
    fun removeAt(viewHistoryRepository: ViewHistoryRepository, index: Int): Job {
        return remove(get(index), index)
    }

    fun remove(item: UrlItem, passedIndex: Int = -1): Job {
        return CoroutineScope(Dispatchers.Main).launch {
            val index = if (passedIndex == -1) suggestions.indexOf(item) else passedIndex
            withContext(Dispatchers.IO) {
                //TODO consider it. viewHistoryRepository.delete(item)
                suggestions.remove(item)
            }

            notifyItemRemoved(index)
        }
    }

}