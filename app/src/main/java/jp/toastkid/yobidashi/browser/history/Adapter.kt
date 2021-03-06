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
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.lib.BrowserViewModel
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
) : RecyclerView.Adapter<ViewHolder>() {

    /**
     * Layout inflater.
     */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val items = mutableListOf<ViewHistory>()

    private val parent = Job()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(DataBindingUtil.inflate(inflater, ITEM_LAYOUT_ID, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewHistory: ViewHistory = items[position]

        holder.setText(
                viewHistory.title,
                viewHistory.url,
                DateFormat.format(context.getString(R.string.date_format), viewHistory.lastViewed).toString() ?: ""
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

        holder.itemView.setOnLongClickListener { v ->
            browserViewModel?.openBackground(viewHistory.title, Uri.parse(viewHistory.url))
            true
        }
    }

    override fun getItemCount(): Int = items.size

    fun filter(query: String?) {
        if (query.isNullOrBlank()) {
            refresh()
            return
        }

        items.clear()

        CoroutineScope(Dispatchers.Main).launch(parent) {
            withContext(Dispatchers.IO) {
                items.addAll(viewHistoryRepository.search("%$query%"))
            }

            notifyDataSetChanged()
        }
    }

    fun refresh(onComplete: () -> Unit = {}) {
        items.clear()

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) { items.addAll(viewHistoryRepository.reversed()) }

            notifyDataSetChanged()
            onComplete()
        }
    }

    /**
     * Remove item with position.
     *
     * @param position
     */
    fun removeAt(position: Int) {
        remove(items[position], position)
    }

    private fun remove(item: ViewHistory, position: Int = -1) {
        CoroutineScope(Dispatchers.Main).launch {
            val index = if (position == -1) items.indexOf(item) else position
            withContext(Dispatchers.IO) {
                viewHistoryRepository.delete(item)
                items.remove(item)
            }

            notifyItemRemoved(index)
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
            notifyDataSetChanged()
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
