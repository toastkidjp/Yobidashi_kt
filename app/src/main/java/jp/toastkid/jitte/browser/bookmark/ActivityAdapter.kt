package jp.toastkid.jitte.browser.bookmark

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
import jp.toastkid.jitte.R
import jp.toastkid.jitte.browser.bookmark.model.Bookmark
import jp.toastkid.jitte.browser.bookmark.model.Bookmark_Relation
import jp.toastkid.jitte.databinding.ItemBookmarkBinding
import timber.log.Timber

/**
 * @author toastkidjp
 */
internal class ActivityAdapter(
        context: Context,
        private val relation: Bookmark_Relation,
        private val onClick: (Bookmark) -> Unit,
        private val onDelete: (Bookmark) -> Unit
) : RecyclerView.Adapter<ViewHolder> () {

    /** Items. */
    private val items: MutableList<Bookmark> = mutableListOf()

    /** Layout inflater.  */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val disposables: CompositeDisposable = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate<ItemBookmarkBinding>(
                inflater, R.layout.item_bookmark, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookmark: Bookmark = items.get(position)
        holder.setText(bookmark.title, bookmark.url)
        holder.itemView.setOnClickListener { v ->
            if (bookmark.folder) {
                query(bookmark.title)
            } else {
                onClick(bookmark)
            }
        }
        holder.setOnClickAdd(bookmark) { item ->
            remove(item)
            onDelete.invoke(item)
        }

        if (bookmark.folder) {
            holder.setImageId(R.drawable.ic_folder_black)
        } else {
            holder.setImage(bookmark.favicon)
        }

        disposables.add(holder.setImage(bookmark.favicon))

        holder.itemView.setOnLongClickListener { v ->
            val context = v.context
            AlertDialog.Builder(context)
                    .setTitle(R.string.delete)
                    .setMessage(Html.fromHtml(context.getString(R.string.confirm_clear_all_settings)))
                    .setCancelable(true)
                    .setNegativeButton(R.string.cancel) { d, i -> d.cancel() }
                    .setPositiveButton(R.string.ok) { d, i ->
                        remove(bookmark)
                        d.dismiss()
                    }
                    .show()
            true
        }
        holder.switchDividerVisibility(position != (itemCount - 1))
    }

    fun showRoot() {
        query("root")
    }

    fun query(title: String) {
        relation.selector()
                .parentEq(title)
                .executeAsObservable()
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { items.clear() }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate { notifyDataSetChanged()
                Timber.i("doOnTerminate")}
                .observeOn(Schedulers.computation())
                .subscribe{ items.add(it)
                Timber.i("${it.title} ${it.url} ${items.size}")
                }
    }

    /**
     * Remove item with position.
     * @param position
     */
    fun removeAt(position: Int) {
        remove(items.get(position))
    }

    /**
     * Remove item.
     * @param position
     */
    fun remove(item: Bookmark) {
        val pos = items.indexOf(item)
        relation.deleteAsMaybe(item)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .doOnComplete { items.remove(item) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { i -> notifyItemRemoved(pos) }
    }

    fun clearAll(onComplete: () -> Unit) {
        relation.deleter()
                .executeAsSingle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { i ->
                    onComplete()
                    notifyItemRangeRemoved(0, i)
                }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun dispose() {
        disposables.dispose()
    }

}
