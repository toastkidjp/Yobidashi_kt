package jp.toastkid.yobidashi.search.history

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemSearchHistoryBinding
import jp.toastkid.yobidashi.search.SearchAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.ArrayList
import kotlin.math.min

/**
 * ModuleAdapter of search history list.
 *
 * @param context
 * @param repository Relation
 * @param onClick On click callback
 * @param onVisibilityChanged On changed visibility callback
 * @param onClickAdd
 *
 * @author toastkidjp
 */
internal class ModuleAdapter(
        context: Context,
        private val repository: SearchHistoryRepository,
        private val onClick: (SearchHistory) -> Unit,
        private val onVisibilityChanged: (Boolean) -> Unit,
        private val onClickAdd: (SearchHistory) -> Unit,
        private val useAddition: Boolean = true,
        private val maxItemCount: Int = -1
) : RecyclerView.Adapter<ViewHolder>(), Removable {

    /**
     * Layout inflater.
     */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    /**
     * Selected items.
     */
    private val selected: MutableList<SearchHistory> = ArrayList(5)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemSearchHistoryBinding>(
                inflater, R.layout.item_search_history, parent, false)
        binding.searchHistoryAdd.isVisible = useAddition
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val searchHistory = selected[position]
        holder.hideButton()
        holder.setText(searchHistory.query)
        holder.itemView.setOnClickListener {
            try {
                onClick(searchHistory)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        holder.setOnClickAdd(searchHistory, onClickAdd)

        holder.setOnClickDelete { remove(searchHistory) }

        holder.setAddIcon(R.drawable.ic_add_circle_search)

        holder.setImageRes(jp.toastkid.search.SearchCategory.findByCategory(searchHistory.category as String).iconId)
        holder.itemView.setOnLongClickListener { v ->
            SearchAction(
                    v.context,
                    searchHistory.category ?: "",
                    searchHistory.query ?: "",
                    onBackground = true,
                    saveHistory = true
            ).invoke()
            true
        }
        holder.setFavorite(searchHistory.category as String, searchHistory.query as String)
    }

    /**
     * Clear all items.
     *
     * @param onComplete Callback
     * @return [Job]
     */
    fun clearAll(onComplete: () -> Unit): Job =
            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.IO) {
                    repository.deleteAll()
                    selected.clear()
                }

                onComplete()
                notifyDataSetChanged()
            }

    fun refresh(onEmpty: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            val items = withContext(Dispatchers.IO) { repository.findAll() }
            if (items.isEmpty()) {
                onEmpty()
                return@launch
            }
            selected.addAll(items)
            notifyDataSetChanged()
        }
    }

    /**
     * Execute query.
     *
     * @param s
     * @return [Job]
     */
    fun query(s: CharSequence): Job {
        clear()

        return CoroutineScope(Dispatchers.Main).launch {
            val items = withContext(Dispatchers.IO) {
                if (s.isNotBlank()) {
                    repository.select("$s%")
                } else {
                    repository.findAll()
                }
            }
            items.forEach { add(it) }
            onVisibilityChanged(!isEmpty)
            notifyDataSetChanged()
        }
    }

    override fun removeAt(position: Int): Job {
        return remove(selected[position])
    }

    /**
     * Remove item with position.
     *
     * @param item [SearchHistory]
     * @return [Job]
     */
    private fun remove(item: SearchHistory): Job {
        return CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                repository.delete(item)
            }

            val index = selected.indexOf(item)
            selected.remove(item)
            notifyItemRemoved(index)
            if (isEmpty) {
                onVisibilityChanged(false)
            }
        }
    }

    /**
     * Return selected item is empty.
     *
     * @return If this adapter's item is zero, return true.
     */
    private val isEmpty: Boolean
        get() = itemCount == 0

    /**
     * Clear selected items.
     */
    fun clear() {
        selected.clear()
    }

    /**
     * Add passed history item to selected list.
     * @param history
     */
    private fun add(history: SearchHistory) {
        selected.add(history)
    }

    override fun getItemCount(): Int {
        return if (maxItemCount == -1) selected.size else min(maxItemCount, selected.size)
    }
}
