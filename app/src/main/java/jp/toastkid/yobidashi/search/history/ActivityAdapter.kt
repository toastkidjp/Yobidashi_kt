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
import jp.toastkid.yobidashi.search.BackgroundSearchAction
import jp.toastkid.yobidashi.search.SearchCategory
import timber.log.Timber

/**
 * ModuleAdapter of search history list.
 *
 * @param context [Context]
 * @param relation Relation
 * @param onClick On click callback
 * @param onDelete On delete callback
 *
 * @author toastkidjp
 */
internal class ActivityAdapter(
        context: Context,
        private val relation: SearchHistory_Relation,
        private val onClick: (SearchHistory) -> Unit,
        private val onDelete: (SearchHistory) -> Unit
) : OrmaRecyclerViewAdapter<SearchHistory, ViewHolder>(context, relation), Removable {

    /** Layout inflater.  */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(
                inflater, R.layout.item_search_history, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val searchHistory: SearchHistory? = getItem(position)
        searchHistory?.let {
            it.query?.let { holder.setText(it) }
            holder.itemView.setOnClickListener { _ ->
                try {
                    onClick(it)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }

            holder.setOnClickDelete { removeAt(position) }

            holder.setFavorite(it.category as String, it.query as String)
            holder.setImageRes(SearchCategory.findByCategory(it.category as String).iconId)
            holder.itemView.setOnLongClickListener { v ->
                BackgroundSearchAction(v, it.category, it.query, false).invoke()
                true
            }
        }

        holder.hideAddButton()
        holder.switchDividerVisibility(position != (itemCount - 1))
    }

    /**
     * Remove item with position.
     * @param position
     */
    override fun removeAt(position: Int): Disposable {
        val item = getItem(position)
        return removeItemAsMaybe(item)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    notifyItemRemoved(position)
                }
    }

    fun clearAll(onComplete: () -> Unit): Disposable {
        return clearAsSingle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { i ->
                    onComplete()
                    notifyItemRangeRemoved(0, i)
                }
    }

}
