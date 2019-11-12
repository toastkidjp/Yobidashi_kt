package jp.toastkid.yobidashi.search.history

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Completable
import io.reactivex.Maybe
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
 * @param repository repository
 * @param onClick On click callback
 * @param onDelete On delete callback
 *
 * @author toastkidjp
 */
internal class ActivityAdapter(
        context: Context,
        private val repository: SearchHistoryRepository,
        private val onClick: (SearchHistory) -> Unit,
        private val onDelete: (SearchHistory) -> Unit
) : RecyclerView.Adapter<ViewHolder>(), Removable {

    /**
     * Layout inflater.
     */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val items = mutableListOf<SearchHistory>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(
                inflater, R.layout.item_search_history, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items.get(position).let { searchHistory ->
            searchHistory.query?.let { holder.setText(it) }
            holder.itemView.setOnClickListener { _ ->
                try {
                    onClick(searchHistory)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }

            holder.setOnClickDelete { removeAt(position) }

            holder.setFavorite(searchHistory.category ?: "", searchHistory.query ?: "")
            holder.setImageRes(SearchCategory.findByCategory(searchHistory.category ?: "").iconId)
            holder.itemView.setOnLongClickListener { v ->
                BackgroundSearchAction(v, searchHistory.category, searchHistory.query, false).invoke()
                true
            }
        }

        holder.hideAddButton()
        holder.switchDividerVisibility(position != (itemCount - 1))
    }

    /**
     * Remove item with position.
     *
     * @param position Removing item's position
     * @return [Disposable]
     */
    override fun removeAt(position: Int): Disposable =
            Completable.fromAction {
                val searchHistory = items[position]
                repository.delete(searchHistory)
                items.remove(searchHistory)
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { notifyItemRemoved(position) }

    /**
     * Clear all items.
     *
     * @param onComplete Callback
     * @return [Disposable]
     */
    fun clearAll(onComplete: () -> Unit): Disposable =
            Completable.fromAction {
                repository.deleteAll()
                items.clear()
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            onComplete()
                            notifyDataSetChanged()
                        },
                        Timber::e
                )

    fun refresh(): Disposable {
        return Maybe.fromCallable { repository.findAll() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            items.addAll(it)
                            notifyDataSetChanged()
                        },
                        Timber::e
                )
    }

    override fun getItemCount() = items.size
}
