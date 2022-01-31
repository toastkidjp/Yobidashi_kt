package jp.toastkid.yobidashi.search.history

import android.content.Context
import android.graphics.Color
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import jp.toastkid.lib.color.IconColorFinder
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.list.CommonItemCallback
import jp.toastkid.lib.view.swipe.Removable
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemSearchHistoryBinding
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.SearchFragmentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.min

/**
 * ModuleAdapter of search history list.
 *
 * @param context
 * @param repository Relation
 * @param onVisibilityChanged On changed visibility callback
 * @param useAddition
 * @param maxItemCount Use for limiting display item count
 *
 * @author toastkidjp
 */
internal class ModuleAdapter(
        context: Context,
        private val repository: SearchHistoryRepository,
        private val onVisibilityChanged: (Boolean) -> Unit,
        private val useAddition: Boolean = true,
        private val maxItemCount: Int = -1
) : ListAdapter<SearchHistory, ViewHolder>(
    CommonItemCallback.with({ a, b -> a.key == b.key }, { a, b -> a == b })
), Removable {

    /**
     * Layout inflater.
     */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private var viewModel: SearchFragmentViewModel? = null

    @ColorInt
    private var iconColor = Color.TRANSPARENT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemSearchHistoryBinding>(
                inflater, LAYOUT_ID, parent, false)
        binding.searchHistoryAdd.isVisible = useAddition
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val searchHistory = getItem(position)
        holder.hideButton()
        val context = holder.itemView.context
        holder.setText(
            searchHistory.query,
            DateFormat.format(context.getString(R.string.date_format), searchHistory.timestamp)
        )
        holder.itemView.setOnClickListener {
            try {
                val query = searchHistory.query ?: return@setOnClickListener
                val category = searchHistory.category ?: return@setOnClickListener
                viewModel?.searchWithCategory(query, category)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        holder.setOnClickAdd(searchHistory) {
            val query = it.query ?: return@setOnClickAdd
            viewModel?.putQuery(query)
        }

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

        if (iconColor == Color.TRANSPARENT) {
            iconColor = IconColorFinder(
                    holder.itemView.resources.configuration,
                    PreferenceApplier(holder.itemView.context)
            ).invoke()
        }

        holder.setIconColor(iconColor)
        holder.hideButton()
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
                }

                onComplete()
                submitList(emptyList())
            }

    fun refresh(onEmpty: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            val items = withContext(Dispatchers.IO) { repository.findAll() }
            if (items.isEmpty()) {
                onEmpty()
                return@launch
            }
            submitList(items)
        }
    }

    /**
     * Execute query.
     *
     * @param s
     * @return [Job]
     */
    fun query(s: CharSequence): Job {
        return CoroutineScope(Dispatchers.Main).launch {
            val items = withContext(Dispatchers.IO) {
                if (s.isNotBlank()) {
                    repository.select("$s%")
                } else {
                    repository.findAll()
                }
            }
            onVisibilityChanged(items.isNotEmpty())
            submitList(items)
        }
    }

    override fun removeAt(position: Int): Job {
        return remove(getItem(position))
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

            val copy = ArrayList<SearchHistory>(currentList)
            copy.remove(item)
            submitList(copy)
            if (isEmpty()) {
                onVisibilityChanged(false)
            }
        }
    }

    /**
     * Return selected item is empty.
     *
     * @return If this adapter's item is zero, return true.
     */
    private fun isEmpty(): Boolean = currentList.isEmpty()

    /**
     * Clear selected items.
     */
    fun clear() {
        submitList(emptyList())
    }

    fun setViewModel(viewModel: SearchFragmentViewModel) {
        this.viewModel = viewModel
    }

    override fun getItemCount(): Int {
        return if (maxItemCount == -1) currentList.size else min(maxItemCount, currentList.size)
    }

    companion object {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.item_search_history

    }

}
