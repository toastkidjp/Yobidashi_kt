package jp.toastkid.yobidashi.browser.history

import android.content.Context
import android.databinding.DataBindingUtil
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.tab.BackgroundTabQueue
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * View history activity's adapter.
 *
 * @param context
 * @param relation
 * @param onClick
 * @param onDelete
 *
 * @author toastkidjp
 */
internal class ActivityAdapter(
        private val context: Context,
        private val relation: ViewHistory_Relation,
        private val onClick: (ViewHistory) -> Unit,
        private val onDelete: (ViewHistory) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    /**
     * Layout inflater.
     */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

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
        val viewHistory: ViewHistory = relation.reversed()[position]

        holder.setText(
                viewHistory.title,
                viewHistory.url,
                dateFormat.get().format(viewHistory.last_viewed)
        )
        holder.itemView.setOnClickListener { onClick(viewHistory) }
        holder.setOnClickAdd(viewHistory) { history ->
            removeAt(position)
            onDelete.invoke(history)
        }
        holder.setOnClickBookmark(viewHistory)

        holder.setImage(viewHistory.favicon).addTo(disposables)

        holder.itemView.setOnLongClickListener { v ->
            BackgroundTabQueue.add(viewHistory.title, Uri.parse(viewHistory.url))
            Toaster.snackShort(
                    v,
                    v.context.getString(R.string.message_background_tab, viewHistory.title),
                    PreferenceApplier(v.context).colorPair()
            )
            true
        }
        holder.switchDividerVisibility(position != (itemCount - 1))
    }

    override fun getItemCount(): Int = relation.reversed().size

    /**
     * Remove item with position.
     *
     * @param position
     */
    fun removeAt(position: Int) {
        val item = relation.reversed()[position]
        relation.deleteAsMaybe(item)
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
        relation.deleter().executeAsSingle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { i ->
                    onComplete()
                    notifyItemRangeRemoved(0, i)
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
