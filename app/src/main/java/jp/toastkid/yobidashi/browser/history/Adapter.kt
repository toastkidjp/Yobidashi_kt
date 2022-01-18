package jp.toastkid.yobidashi.browser.history

import android.content.Context
import android.net.Uri
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ListAdapter
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.view.list.CommonItemCallback
import jp.toastkid.yobidashi.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * View history activity's adapter.
 *
 * @param context
 * @param viewHistoryRepository
 * @param onClick
 * @param onDelete
 *
 * @author toastkidjp
 */
internal class Adapter(
        private val context: Context,
        private val viewHistoryRepository: ViewHistoryRepository,
        private val onClick: (ViewHistory) -> Unit,
        private val onDelete: (ViewHistory) -> Unit
) : ListAdapter<ViewHistory, ViewHolder>(
    CommonItemCallback.with({ a, b -> a._id == b._id }, { a, b -> a == b })
) {

    /**
     * Layout inflater.
     */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val items = mutableListOf<ViewHistory>()

    private val parent = Job()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(DataBindingUtil.inflate(inflater, ITEM_LAYOUT_ID, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewHistory: ViewHistory = getItem(position)

        holder.setText(
            viewHistory.title,
            viewHistory.url,
            DateFormat.format(context.getString(R.string.date_format), viewHistory.lastViewed).toString()
        )
        holder.itemView.setOnClickListener { onClick(viewHistory) }
        holder.setOnClickAdd(viewHistory) { history ->
            remove(viewHistory)
            onDelete.invoke(history)
        }
        holder.setOnClickBookmark(viewHistory)

        holder.setImage(viewHistory.favicon)

        val browserViewModel = (holder.itemView.context as? FragmentActivity)?.let {
            ViewModelProvider(it).get(BrowserViewModel::class.java)
        }

        holder.itemView.setOnLongClickListener {
            browserViewModel?.openBackground(viewHistory.title, Uri.parse(viewHistory.url))
            true
        }
        holder.hideButton()
    }

    fun filter(query: String?) {
        if (query.isNullOrBlank()) {
            refresh()
            return
        }


        CoroutineScope(Dispatchers.Main).launch(parent) {
            val searchResult = withContext(Dispatchers.IO) {
                viewHistoryRepository.search("%$query%")
            }

            submitList(searchResult)
        }
    }

    fun refresh(onComplete: () -> Unit = {}) {
        CoroutineScope(Dispatchers.Main).launch {
            val items = withContext(Dispatchers.IO) { viewHistoryRepository.reversed() }

            submitList(items)
            onComplete()
        }
    }

    /**
     * Remove item with position.
     *
     * @param position
     */
    fun removeAt(position: Int) {
        remove(getItem(position), position)
    }

    private fun remove(item: ViewHistory, position: Int = -1) {
        CoroutineScope(Dispatchers.Main).launch {
            val index = if (position == -1) currentList.indexOf(item) else position
            val copy = mutableListOf<ViewHistory>().also { it.addAll(currentList) }
            withContext(Dispatchers.IO) {
                viewHistoryRepository.delete(item)
                copy.remove(item)
            }

            submitList(copy)
        }
    }

    /**
     * Clear all item.
     *
     * @param onComplete
     */
    fun clearAll(onComplete: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                viewHistoryRepository.deleteAll()
            }

            onComplete()
            submitList(emptyList())
        }
    }

    fun dispose() {
        parent.cancel()
    }

    companion object {

        @LayoutRes
        private val ITEM_LAYOUT_ID = R.layout.item_view_history

    }

}
