package jp.toastkid.yobidashi.search.history

import android.content.Context
import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.github.gfx.android.orma.widget.OrmaRecyclerViewAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemSearchHistoryBinding
import jp.toastkid.yobidashi.search.BackgroundSeachAction
import jp.toastkid.yobidashi.search.SearchCategory
import timber.log.Timber
import java.util.*

/**
 * ModuleAdapter of search history list.
 *
 * @param context
 * @param relation Relation
 * @param onClick On click callback
 * @param onVisibilityChanged On changed visibility callback
 * @param onClickAdd
 *
 * @author toastkidjp
 */
internal class ModuleAdapter(
        context: Context,
        private val relation: SearchHistory_Relation,
        private val onClick: (SearchHistory) -> Unit,
        private val onVisibilityChanged: (Boolean) -> Unit,
        private val onClickAdd: (SearchHistory) -> Unit
) : OrmaRecyclerViewAdapter<SearchHistory, ViewHolder>(context, relation) {

    /** Layout inflater.  */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    /** Selected items.  */
    private val selected: MutableList<SearchHistory> = ArrayList<SearchHistory>(5)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate<ItemSearchHistoryBinding>(
                inflater, R.layout.item_search_history, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val searchHistory = selected[position]
        holder.setText(searchHistory.query!!)
        holder.itemView.setOnClickListener { v ->
            try {
                onClick(searchHistory)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        holder.setOnClickAdd(searchHistory, onClickAdd)

        holder.setAddIcon(R.drawable.ic_add_circle_search)

        holder.setImageRes(SearchCategory.findByCategory(searchHistory.category as String).iconId)
        holder.itemView.setOnLongClickListener { v ->
            BackgroundSeachAction(v, searchHistory.category, searchHistory.query).invoke()
            true
        }
        holder.setFavorite(searchHistory.category as String, searchHistory.query as String)
        holder.switchDividerVisibility(position != (itemCount - 1))
    }

    /**
     * Execute query.
     * @param s
     *
     * @return
     */
    fun query(s: CharSequence): Disposable {

        clear()

        val selector = relation.selector()
        if (s.isNotEmpty()) {
            selector.where(relation.schema.query, "LIKE", s.toString() + "%")
        }
        return selector
                .orderByTimestampDesc()
                .limit(5)
                .executeAsObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate {
                    onVisibilityChanged(!isEmpty)
                    notifyDataSetChanged()
                }
                .subscribe { it -> this.add(it) }
    }

    /**
     * Remove item with position.
     * @param position
     */
    fun removeAt(position: Int): Disposable {
        val item = selected[position]
        return removeItemAsMaybe(item)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { i ->
                    selected.remove(item)
                    notifyItemRemoved(position)
                    if (isEmpty) {
                        onVisibilityChanged(false)
                    }
                }
    }

    /**
     * Return selected item is empty.
     * @return
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
        return selected.size
    }
}
