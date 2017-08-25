package jp.toastkid.jitte.browser.history

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import com.github.gfx.android.orma.widget.OrmaRecyclerViewAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.jitte.R
import jp.toastkid.jitte.databinding.ItemViewHistoryBinding

/**
 * @author toastkidjp
 */
internal class ActivityAdapter(
        context: Context,
        relation: ViewHistory_Relation,
        private val onClick: (ViewHistory) -> Unit,
        private val onDelete: (ViewHistory) -> Unit
) : OrmaRecyclerViewAdapter<ViewHistory, ViewHolder>(context, relation) {

    /** Layout inflater.  */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val disposables: CompositeDisposable = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate<ItemViewHistoryBinding>(
                inflater, R.layout.item_view_history, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewHistory: ViewHistory = getItem(position)
        holder.setText(viewHistory.title, viewHistory.url)
        holder.itemView.setOnClickListener { v -> onClick(viewHistory) }
        holder.setOnClickAdd(viewHistory) { history ->
            removeAt(position)
            onDelete.invoke(history)
        }

        disposables.add(holder.setImage(viewHistory.favicon))

        holder.itemView.setOnLongClickListener { v ->
            val context = context
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

    fun dispose() {
        disposables.dispose()
    }

}
