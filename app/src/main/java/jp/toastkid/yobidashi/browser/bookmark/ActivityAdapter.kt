package jp.toastkid.yobidashi.browser.bookmark

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ListAdapter
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.color.IconColorFinder
import jp.toastkid.lib.view.list.CommonItemCallback
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Stack

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
) : ListAdapter<Bookmark, ViewHolder>(
    CommonItemCallback.with({ a, b -> a._id == b._id }, { a, b -> a == b })
) {

    /**
     * Layout inflater.
     */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val disposables: Job by lazy { Job() }

    /**
     * Folder moving history.
     */
    private val folderHistory: Stack<String> = Stack()

    private val iconColor = IconColorFinder.from(context).invoke()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(DataBindingUtil.inflate(inflater, ITEM_LAYOUT_ID, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookmark: Bookmark = getItem(position)
        holder.setText(bookmark.title, bookmark.url)
        holder.setTimeIfNeed(bookmark.lastViewed)
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
            holder.setIconColorFilter(iconColor)
        } else {
            holder.setImage(bookmark.favicon)
            holder.setIconColorFilter(Color.TRANSPARENT)
        }

        val browserViewModel = (holder.itemView.context as? FragmentActivity)?.let {
            ViewModelProvider(it).get(BrowserViewModel::class.java)
        }

        holder.itemView.setOnLongClickListener {
            browserViewModel?.openBackground(bookmark.title, Uri.parse(bookmark.url))
            true
        }
    }

    /**
     * Return current folder name.
     */
    fun currentFolderName(): String =
            if (currentList.isEmpty() && folderHistory.isNotEmpty()) folderHistory.peek()
            else if (currentList.isEmpty()) Bookmark.getRootFolderName()
            else currentList[0].parent

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
            val items = withContext(Dispatchers.IO) {
                bookmarkRepository.findByParent(title)
            }
            submitList(items)
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
        remove(getItem(position))
    }

    /**
     * Remove item.
     *
     * @param item [Bookmark]
     */
    private fun remove(item: Bookmark) {
        val copy = ArrayList<Bookmark>(currentList)
        CoroutineScope(Dispatchers.Main).launch(disposables) {
            withContext(Dispatchers.IO) { bookmarkRepository.delete(item) }

            copy.remove(item)
            submitList(copy)
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
            submitList(emptyList())
        }
    }

    /**
     * Dispose all disposable instances.
     */
    fun dispose() {
        disposables.cancel()
    }

    companion object {

        @LayoutRes
        private const val ITEM_LAYOUT_ID = R.layout.item_view_history

    }

}
