package jp.toastkid.yobidashi.browser.bookmark

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark_Relation
import java.util.*

/**
 * TODO clean up code.
 *
 * @author toastkidjp
 */
internal class ActivityAdapter(
        context: Context,
        private val relation: Bookmark_Relation,
        private val onClick: (Bookmark) -> Unit,
        private val onDelete: (Bookmark) -> Unit
) : RecyclerView.Adapter<ViewHolder> () {

    /**
     * Items.
     */
    private val items: MutableList<Bookmark> = mutableListOf()

    /**
     * Layout inflater.
     */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    /**
     * [CompositeDisposable].
     */
    private val disposables: CompositeDisposable = CompositeDisposable()

    /**
     * Folder moving history.
     */
    private val folderHistory: Stack<String> = Stack()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(DataBindingUtil.inflate(inflater, R.layout.item_bookmark, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookmark: Bookmark = items.get(position)
        holder.setText(bookmark.title, bookmark.url)
        holder.itemView.setOnClickListener { v ->
            if (bookmark.folder) {
                folderHistory.push(bookmark.parent)
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
            holder.setImage(bookmark.favicon).addTo(disposables)
        }

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

    /**
     * Return current folder name.
     */
    fun currentFolderName(): String =
            if (items.isEmpty() && folderHistory.isNotEmpty()) folderHistory.peek()
            else if (items.isEmpty()) Bookmarks.ROOT_FOLDER_NAME
            else items.get(0).parent

    /**
     * Back to previous folder.
     */
    fun back(): Boolean {
        if (folderHistory.isEmpty()) {
            return false
        }
        query(folderHistory.pop())
        return true
    }

    /**
     * Show root folder.
     */
    fun showRoot() {
        query(Bookmarks.ROOT_FOLDER_NAME)
    }

    /**
     * Query with specified title.
     *
     * @param title
     */
    fun query(title: String) {
        relation.selector()
                .parentEq(title)
                .executeAsObservable()
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { items.clear() }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate {
                    notifyDataSetChanged()
                }
                .observeOn(Schedulers.computation())
                .subscribe{ items.add(it) }
                .addTo(disposables)
    }

    /**
     * Reload.
     */
    fun reload() {
        query(currentFolderName())
    }

    /**
     * Remove item with position.
     * @param position
     */
    fun removeAt(position: Int) {
        remove(items.get(position), position)
    }

    /**
     * Remove item.
     *
     * @param item [Bookmark]
     * @param position position
     */
    fun remove(item: Bookmark, position: Int = items.indexOf(item)) {
        relation.deleteAsMaybe(item)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { i ->
                    items.remove(item)
                    notifyItemRemoved(position)
                }
                .addTo(disposables)
    }

    /**
     * Clear all items.
     *
     * @param onComplete callback
     */
    fun clearAll(onComplete: () -> Unit) {
        relation.deleter()
                .executeAsSingle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { i ->
                    onComplete()
                    items.clear()
                    notifyItemRangeRemoved(0, i)
                }
                .addTo(disposables)
    }

    override fun getItemCount(): Int = items.size

    /**
     * Dispose all disposable instances.
     */
    fun dispose() {
        disposables.clear()
    }

}
