package jp.toastkid.yobidashi.search.url_suggestion

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.history.ViewHistory
import jp.toastkid.yobidashi.browser.history.ViewHistoryRepository
import timber.log.Timber

/**
 * URL suggestion module's adapter.
 *
 * @param context [Context]
 * @param browseCallback
 * @param browseBackgroundCallback
 * @author toastkidjp
 */
class Adapter(
        context: Context,
        private val removeAt: (Int) -> Unit,
        private val browseCallback: (String) -> Unit,
        private val browseBackgroundCallback: (String) -> Unit
): RecyclerView.Adapter<ViewHolder>() {

    /**
     * Item list.
     */
    private val suggestions: MutableList<ViewHistory> = mutableListOf()

    /**
     * Layout inflater.
     */
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(DataBindingUtil.inflate(
                layoutInflater, R.layout.item_bookmark, parent, false
        ))
        return holder;
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: ViewHistory = suggestions.get(position)
        holder.setTitle(item.title)
        holder.setUrl(item.url)
        holder.setOnClick(View.OnClickListener { browseCallback(item.url) })
        holder.setOnLongClick(View.OnLongClickListener {
            browseBackgroundCallback(item.url)
            true
        })
        holder.setDelete(View.OnClickListener { removeAt(position) })
    }

    override fun getItemCount(): Int = suggestions.size

    /**
     * Add item to list.
     *
     * @param item
     */
    fun add(item: ViewHistory?) {
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
    fun get(index: Int): ViewHistory = suggestions.get(index)

    /**
     * Remove at index.
     *
     * @param viewHistoryRepository
     * @param index
     * @return disposable
     */
    fun removeAt(viewHistoryRepository: ViewHistoryRepository, index: Int): Disposable {
        return Completable.fromAction {
            val item = get(index)
            viewHistoryRepository.delete(item)
            suggestions.remove(item)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { notifyItemRemoved(index) },
                        Timber::e
                )
    }
}