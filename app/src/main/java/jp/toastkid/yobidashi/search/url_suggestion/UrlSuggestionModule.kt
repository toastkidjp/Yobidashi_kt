package jp.toastkid.yobidashi.search.url_suggestion

import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.browser.UrlItem
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import jp.toastkid.yobidashi.browser.history.ViewHistoryRepository
import jp.toastkid.yobidashi.databinding.ModuleUrlSuggestionBinding
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.search.history.SwipeActionAttachment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class UrlSuggestionModule(
        private val binding: ModuleUrlSuggestionBinding,
        browseCallback: (String) -> Unit,
        browseBackgroundCallback: (String) -> Unit
) {

    /**
     * Adapter.
     */
    private val adapter = Adapter(
            LayoutInflater.from(binding.root.context),
            this::remove,
            browseCallback,
            browseBackgroundCallback
            )

    var enable = true

    /**
     * Bookmark's database repository.
     */
    private val bookmarkRepository: BookmarkRepository =
            DatabaseFinder().invoke(binding.root.context).bookmarkRepository()

    /**
     * Database repository.
     */
    private val viewHistoryRepository: ViewHistoryRepository =
            DatabaseFinder().invoke(binding.root.context).viewHistoryRepository()

    init {
        binding.urlSuggestions.adapter = adapter
        binding.urlSuggestions.layoutManager =
                LinearLayoutManager(binding.root.context, RecyclerView.VERTICAL, false)
        SwipeActionAttachment().invoke(binding.urlSuggestions)
    }

    /**
     * Remove item.
     *
     * @param index
     */
    private fun removeAt(index: Int) {
        adapter.removeAt(viewHistoryRepository, index)
    }

    private fun remove(item: UrlItem) {
        adapter.remove(item)
    }

    /**
     * Query to database.
     *
     * @param q query string
     */
    fun query(q: CharSequence) {
        adapter.clear()

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                if (q.isBlank()) {
                    return@withContext
                }
                bookmarkRepository.search("%$q%", ITEM_LIMIT).forEach { adapter.add(it) }
            }

            withContext(Dispatchers.IO) {
                viewHistoryRepository.search("%$q%", ITEM_LIMIT).forEach { adapter.add(it) }
            }

            if (adapter.isNotEmpty()) {
                show()
            } else {
                hide()
            }
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * Show this module.
     */
    fun show() {
        if (!binding.root.isVisible && enable) {
            runOnMainThread { binding.root.isVisible = true }
        }
    }

    /**
     * Hide this module.
     */
    fun hide() {
        if (binding.root.isVisible) {
            runOnMainThread { binding.root.isVisible = false }
        }
    }

    private fun runOnMainThread(action: () -> Unit) =
            CoroutineScope(Dispatchers.Main).launch { action() }

    companion object {

        /**
         * Item limit.
         */
        private const val ITEM_LIMIT = 3
    }
}