package jp.toastkid.yobidashi.browser.history

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemViewHistoryBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author toastkidjp
 */
internal class ActivityAdapter(
        private val context: Context,
        private val relation: ViewHistory_Relation,
        private val onClick: (ViewHistory) -> Unit,
        private val onDelete: (ViewHistory) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {
    override fun getItemCount(): Int = relation.reversed().size

    /** Layout inflater.  */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val disposables: CompositeDisposable = CompositeDisposable()

    private val dateFormat: ThreadLocal<DateFormat> = object: ThreadLocal<DateFormat>() {
        override fun initialValue(): DateFormat =
                SimpleDateFormat(context.getString(R.string.date_format), Locale.getDefault())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate<ItemViewHistoryBinding>(
                inflater, R.layout.item_view_history, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewHistory: ViewHistory = relation.reversed().get(position)

        holder.setText(
                viewHistory.title,
                viewHistory.url,
                dateFormat.get().format(viewHistory.last_viewed)
        )
        holder.itemView.setOnClickListener { v -> onClick(viewHistory) }
        holder.setOnClickAdd(viewHistory) { history ->
            removeAt(position)
            onDelete.invoke(history)
        }
        holder.setOnClickBookmark(viewHistory)

        disposables.add(holder.setImage(viewHistory.favicon))

        holder.itemView.setOnLongClickListener { v ->
            AlertDialog.Builder(context)
                    .setTitle(R.string.delete)
                    .setMessage(Html.fromHtml(context.getString(R.string.confirm_clear_all_settings)))
                    .setCancelable(true)
                    .setNegativeButton(R.string.cancel) { d, i -> d.cancel() }
                    .setPositiveButton(R.string.ok) { d, i ->
                        removeAt(position)
                        d.dismiss()
                    }
                    .show()
            true
        }
        holder.switchDividerVisibility(position != (itemCount - 1))
    }

    /**
     * Remove item with position.
     * @param position
     */
    fun removeAt(position: Int) {
        val item = relation.reversed().get(position)
        disposables.add(
                relation.deleteAsMaybe(item)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { i -> notifyItemRemoved(position) }
        )
    }

    fun clearAll(onComplete: () -> Unit) {
        disposables.add(
                relation.deleter().executeAsSingle()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { i ->
                            onComplete()
                            notifyItemRangeRemoved(0, i)
                        }
        )
    }

    fun dispose() {
        disposables.dispose()
    }

}
