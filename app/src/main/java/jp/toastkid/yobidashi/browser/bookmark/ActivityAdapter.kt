package jp.toastkid.yobidashi.browser.bookmark

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private val disposables: Job by lazy { Job() }

    /**
     * Folder moving history.
     */
    private val folderHistory: Stack<String> = Stack()

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
            holder.setImage(bookmark.favicon)
        }

        val browserViewModel = (holder.itemView.context as? FragmentActivity)?.let {
            ViewModelProvider(it).get(BrowserViewModel::class.java)
        }

        holder.itemView.setOnLongClickListener { v ->
            browserViewModel?.openBackground(bookmark.title, Uri.parse(bookmark.url))
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

    private fun findByFolderName(title: String) {
        CoroutineScope(Dispatchers.Main).launch(disposables) {
            withContext(Dispatchers.IO) {
                items.clear()
                bookmarkRepository.findByParent(title).forEach { items.add(it) }
            }
            notifyDataSetChanged()
            onRefresh()
        }
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
        CoroutineScope(Dispatchers.Main).launch(disposables) {
            withContext(Dispatchers.IO) { bookmarkRepository.delete(item) }

            items.remove(item)
            notifyItemRemoved(position)
        }
    }

    /**
     * Clear all items.
     *
     * @param onComplete callback
     */
    fun clearAll(onComplete: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch(disposables) {
            withContext(Dispatchers.IO) { bookmarkRepository.clear() }

            onComplete()
            items.clear()
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = items.size

    /**
     * Dispose all disposable instances.
     */
    fun dispose() {
        disposables.cancel()
    }

}
