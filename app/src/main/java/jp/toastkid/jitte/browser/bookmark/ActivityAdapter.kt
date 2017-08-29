package jp.toastkid.jitte.browser.bookmark

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
import jp.toastkid.jitte.browser.bookmark.model.Bookmark
import jp.toastkid.jitte.browser.bookmark.model.Bookmark_Relation
import jp.toastkid.jitte.databinding.ItemViewHistoryBinding

/**
 * @author toastkidjp
 */
internal class ActivityAdapter(
        context: Context,
        private val relation: Bookmark_Relation,
        private val onClick: (Bookmark) -> Unit,
        private val onDelete: (Bookmark) -> Unit
) : OrmaRecyclerViewAdapter<Bookmark, ViewHolder>(context, relation) {

    /** Layout inflater.  */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val disposables: CompositeDisposable = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate<ItemViewHistoryBinding>(
                inflater, R.layout.item_view_history, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookmark: Bookmark = getItem(position)
        holder.setText(bookmark.title, bookmark.url)
        holder.itemView.setOnClickListener { v ->
            if (bookmark.folder) {
                return@setOnClickListener
            } else {
                onClick(bookmark)
            }
        }
        holder.setOnClickAdd(bookmark) { item ->
            removeAt(position)
            onDelete.invoke(item)
        }

        if (bookmark.folder) {
            holder.setImageId(R.drawable.ic_folder_black)
        } else {
            holder.setImage(bookmark.favicon)
        }

        disposables.add(holder.setImage(bookmark.favicon))

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
