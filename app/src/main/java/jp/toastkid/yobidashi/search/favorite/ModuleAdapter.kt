package jp.toastkid.yobidashi.search.favorite

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.search.BackgroundSearchAction
import jp.toastkid.yobidashi.search.SearchCategory
import timber.log.Timber
import java.util.*

/**
 * ModuleAdapter of search history list.
 *
 * @param context
 * @param favoriteSearchRepository Relation
 * @param onClick On click callback
 * @param onVisibilityChanged On changed visibility callback
 * @param onClickAdd
 *
 * @author toastkidjp
 */
internal class ModuleAdapter(
        context: Context,
        private val favoriteSearchRepository: FavoriteSearchRepository,
        private val onClick: (FavoriteSearch) -> Unit,
        private val onVisibilityChanged: (Boolean) -> Unit,
        private val onClickAdd: (FavoriteSearch) -> Unit
) : RecyclerView.Adapter<ModuleViewHolder>() {

    /**
     * Layout inflater.
     */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    /**
     * Selected items.
     */
    private val selected: MutableList<FavoriteSearch> = ArrayList(5)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        return ModuleViewHolder(DataBindingUtil.inflate(
                inflater, R.layout.item_search_history, parent, false))
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val favorite = selected[position]
        holder.setText(favorite.query!!)
        holder.itemView.setOnClickListener {
            try {
                onClick(favorite)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        holder.setOnClickAdd(favorite, onClickAdd)

        holder.setAddIcon(R.drawable.ic_add_circle_search)

        holder.setImageRes(SearchCategory.findByCategory(favorite.category as String).iconId)
        holder.itemView.setOnLongClickListener { v ->
            BackgroundSearchAction(v, favorite.category, favorite.query).invoke()
            true
        }
        holder.switchDividerVisibility(position != (itemCount - 1))
    }

    /**
     * Execute query.
     *
     * @param s query word [String]
     * @return [Disposable]
     */
    fun query(s: CharSequence): Disposable {
        clear()

        return Maybe.fromCallable {
            if (s.isNotBlank()) {
                favoriteSearchRepository.select("$s%")
            } else {
                favoriteSearchRepository.findLast5()
            }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapObservable { Observable.fromIterable(it) }
                .doOnTerminate {
                    onVisibilityChanged(!isEmpty)
                    notifyDataSetChanged()
                }
                .subscribe(this::add)
    }

    /**
     * Remove item with position.
     *
     * @param position
     */
    fun removeAt(position: Int): Disposable {
        val item = selected[position]
        return Completable.fromAction { favoriteSearchRepository.delete(item) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    selected.remove(item)
                    notifyItemRemoved(position)
                    if (isEmpty) {
                        onVisibilityChanged(false)
                    }
                }
    }

    /**
     * Return selected item is empty.
     *
     * @return if this adapter is empty, return true
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
     *
     * @param history
     */
    private fun add(history: FavoriteSearch) {
        selected.add(history)
    }

    override fun getItemCount(): Int {
        return selected.size
    }
}
