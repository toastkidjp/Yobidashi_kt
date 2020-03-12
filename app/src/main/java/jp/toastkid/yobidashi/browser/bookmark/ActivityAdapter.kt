package jp.toastkid.yobidashi.browser.bookmark

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.tab.BackgroundTabQueue
import timber.log.Timber
import java.util.*

/**
 * Bookmark activity's adapter.
 *
 * @author toastkidjp
 */
internal class ActivityAdapter(
        context: Context,
        private val bookmarkRepository: BookmarkRepository,
        private val onClick: (Bookmark) -> Unit,
        private val onDelete: (Bookmark) -> Unit,
        private val onRefresh: () -> Unit
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

    /**
     * Use for run on main thread.
     */
    private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(DataBindingUtil.inflate(inflater, R.layout.item_bookmark, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookmark: Bookmark = items[position]
        holder.setText(bookmark.title, bookmark.url)
        holder.itemView.setOnClickListener {
            if (bookmark.folder) {
                folderHistory.push(bookmark.parent)
                findByFolderName(bookmark.title)
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
            BackgroundTabQueue.add(bookmark.title, Uri.parse(bookmark.url))
            Toaster.snackShort(
                    v,
                    v.context.getString(R.string.message_background_tab, bookmark.title),
                    PreferenceApplier(v.context).colorPair()
            )
            true
        }
    }

    /**
     * Return current folder name.
     */
    fun currentFolderName(): String =
            if (items.isEmpty() && folderHistory.isNotEmpty()) folderHistory.peek()
            else if (items.isEmpty()) Bookmark.getRootFolderName()
            else items[0].parent

    /**
     * Back to previous folder.
     */
    fun back(): Boolean {
        if (folderHistory.isEmpty()) {
            return false
        }
        findByFolderName(folderHistory.pop())
        return true
    }

    /**
     * Show root folder.
     */
    fun showRoot() {
        findByFolderName(Bookmark.getRootFolderName())
    }

    fun search(query: String) {
        displayItems(
                Maybe.fromCallable {
                    bookmarkRepository.search("%$query%")
                }
        )
    }

    fun findByFolderName(title: String) {
        displayItems(Maybe.fromCallable { bookmarkRepository.findByParent(title) })
    }

    /**
     * Query with specified title.
     *
     * @param itemCall
     */
    private fun displayItems(itemCall: Maybe<List<Bookmark>>) {
        itemCall
                .subscribeOn(Schedulers.io())
                .flatMapObservable { it.toObservable() }
                .observeOn(Schedulers.computation())
                .doOnSubscribe { items.clear() }
                .subscribe(
                        { items.add(it) },
                        Timber::e,
                        {
                            mainThreadHandler.post {
                                notifyDataSetChanged()
                                onRefresh()
                            }
                        }
                )
                .addTo(disposables)
    }

    /**
     * Reload.
     */
    fun reload() {
        findByFolderName(currentFolderName())
    }

    /**
     * Remove item with position.
     * @param position
     */
    fun removeAt(position: Int) {
        remove(items[position], position)
    }

    /**
     * Remove item.
     *
     * @param item [Bookmark]
     * @param position position
     */
    fun remove(item: Bookmark, position: Int = items.indexOf(item)) {
        Completable.fromAction { bookmarkRepository.delete(item) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
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
        Completable.fromAction { bookmarkRepository.clear() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    onComplete()
                    items.clear()
                    notifyDataSetChanged()
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
