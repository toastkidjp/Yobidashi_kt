package jp.toastkid.yobidashi.search.url_suggestion

import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.yobidashi.browser.UrlItem
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import jp.toastkid.yobidashi.browser.history.ViewHistoryFragment
import jp.toastkid.yobidashi.browser.history.ViewHistoryRepository
import jp.toastkid.yobidashi.databinding.ModuleUrlSuggestionBinding
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.search.SearchFragmentViewModel
import jp.toastkid.yobidashi.search.history.SwipeActionAttachment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author toastkidjp
 */
class UrlSuggestionModule(
        private val binding: ModuleUrlSuggestionBinding,
        viewModel: SearchFragmentViewModel
) {

    var enable = true

    private val database = DatabaseFinder().invoke(binding.root.context)

    /**
     * Bookmark's database repository.
     */
    private val bookmarkRepository: BookmarkRepository =
            database.bookmarkRepository()

    /**
     * Database repository.
     */
    private val viewHistoryRepository: ViewHistoryRepository =
            database.viewHistoryRepository()

    /**
     * Adapter.
     */
    private val adapter = Adapter(
        LayoutInflater.from(binding.root.context),
        this::remove,
        viewModel,
        ItemDeletionUseCase(bookmarkRepository, viewHistoryRepository)
    )

    private val queryUseCase: QueryUseCase

    init {
        binding.urlSuggestions.adapter = adapter
        binding.module = this
        SwipeActionAttachment().invoke(binding.urlSuggestions)
        queryUseCase = QueryUseCase(adapter, bookmarkRepository, viewHistoryRepository) {
            if (it) show() else hide()
        }
    }

    private fun remove(item: UrlItem) {
        adapter.remove(item)
    }

    fun openHistory() {
        (binding.root.context as? FragmentActivity)?.let {
            ViewModelProvider(it).get(ContentViewModel::class.java)
                .nextFragment(ViewHistoryFragment::class.java)
        }
    }

    /**
     * Query to database.
     *
     * @param q query string
     */
    fun query(q: CharSequence) {
        queryUseCase.invoke(q)
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

}