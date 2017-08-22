package jp.toastkid.jitte.search.history

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import com.github.gfx.android.orma.widget.OrmaRecyclerViewAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jp.toastkid.jitte.R
import jp.toastkid.jitte.databinding.ItemSearchHistoryBinding
import jp.toastkid.jitte.search.SearchCategory

/**
 * ModuleAdapter of search history list.
 *
 * @param context
 * @param relation Relation
 * @param onClick On click callback
 * @param onVisibilityChanged On changed visibility callback
 * @param onDelete
 *
 * @author toastkidjp
 */
internal class ActivityAdapter(
        context: Context,
        private val relation: SearchHistory_Relation,
        private val onClick: (SearchHistory) -> Unit,
        private val onDelete: (SearchHistory) -> Unit
) : OrmaRecyclerViewAdapter<SearchHistory, ViewHolder>(context, relation) {

    /** Layout inflater.  */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate<ItemSearchHistoryBinding>(
                inflater, R.layout.item_search_history, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val searchHistory = getItem(position)
        holder.setText(searchHistory.query!!)
        holder.itemView.setOnClickListener { v ->
            try {
                onClick(searchHistory)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        holder.setOnClickAdd(searchHistory) {history ->
            removeAt(position)
            onDelete.invoke(history)
        }

        holder.setAddIcon(R.drawable.ic_remove_search)

        holder.setImageRes(SearchCategory.findByCategory(searchHistory.category as String).iconId)
        holder.itemView.setOnLongClickListener { v ->
            val context = context
            AlertDialog.Builder(context)
                    .setTitle(R.string.delete)
                    .setMessage(Html.fromHtml(context.getString(R.string.confirm_clear_all_settings)))
                    .setCancelable(true)
                    .setPositiveButton(R.string.ok) { d, i ->
                        removeAt(position)
                        d.dismiss()
                    }
                    .show()
            true
        }
        holder.setFavorite(searchHistory.category as String, searchHistory.query as String)
        holder.switchDividerVisibility(position != (itemCount - 1))
    }

    /**
     * Remove item with position.
     * @param position
     */
    private fun removeAt(position: Int) {
        val item = getItem(position)
        removeItemAsMaybe(item)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { i ->
                    notifyItemRemoved(position)
                }
    }

    fun clearAll(onComplete: () -> Unit) {
        clearAsSingle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { i ->
                    onComplete()
                    notifyItemRangeRemoved(0, i)
                }
    }

}
