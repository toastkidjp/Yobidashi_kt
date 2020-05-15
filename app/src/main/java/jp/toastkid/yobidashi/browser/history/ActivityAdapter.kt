package jp.toastkid.yobidashi.browser.history

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.operators.completable.CompletableLift
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserViewModel
import timber.log.Timber
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

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
internal class ActivityAdapter(
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

    /**
     * Disposables.
     */
    private val disposables: CompositeDisposable = CompositeDisposable()

    /**
     * Date format holder.
     */
    private val dateFormat: ThreadLocal<DateFormat> = object: ThreadLocal<DateFormat>() {
        override fun initialValue(): DateFormat =
                SimpleDateFormat(context.getString(R.string.date_format), Locale.getDefault())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(DataBindingUtil.inflate(inflater, R.layout.item_view_history, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewHistory: ViewHistory = items[position]

        holder.setText(
                viewHistory.title,
                viewHistory.url,
                dateFormat.get()?.format(viewHistory.lastViewed) ?: ""
        )
        holder.itemView.setOnClickListener { onClick(viewHistory) }
        holder.setOnClickAdd(viewHistory) { history ->
            removeAt(position)
            onDelete.invoke(history)
        }
        holder.setOnClickBookmark(viewHistory)

        holder.setImage(viewHistory.favicon).addTo(disposables)

        val browserViewModel = (holder.itemView.context as? FragmentActivity)?.let {
            ViewModelProviders.of(it).get(BrowserViewModel::class.java)
        }

        holder.itemView.setOnLongClickListener { v ->
            browserViewModel?.openBackground(viewHistory.title, Uri.parse(viewHistory.url))
            true
        }
        holder.switchDividerVisibility(position != (itemCount - 1))
    }

    override fun getItemCount(): Int = items.size

    fun refresh(onComplete: () -> Unit = {}) {
        CompletableLift.fromAction { items.addAll(viewHistoryRepository.reversed()) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            notifyDataSetChanged()
                            onComplete()
                        },
                        Timber::e
                )
                .addTo(disposables)
    }

    /**
     * Remove item with position.
     *
     * @param position
     */
    fun removeAt(position: Int) {
        val item = items[position]
        Completable.fromAction {
            viewHistoryRepository.delete(item)
            items.remove(item)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { notifyItemRemoved(position) }
                .addTo(disposables)
    }

    /**
     * Clear all item.
     *
     * @param onComplete
     */
    fun clearAll(onComplete: () -> Unit) {
        Completable.fromAction { viewHistoryRepository.deleteAll() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    onComplete()
                    notifyDataSetChanged()
                }
                .addTo(disposables)
    }

    /**
     * Dispose all disposables.
     */
    fun dispose() {
        disposables.clear()
    }

}
