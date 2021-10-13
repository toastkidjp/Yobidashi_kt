package jp.toastkid.yobidashi.search.url_suggestion

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.UrlItem
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import jp.toastkid.yobidashi.browser.history.ViewHistoryFragment
import jp.toastkid.yobidashi.browser.history.ViewHistoryRepository
import jp.toastkid.yobidashi.databinding.ViewCardUrlSuggestionBinding
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.search.SearchFragmentViewModel
import jp.toastkid.yobidashi.search.history.SwipeActionAttachment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author toastkidjp
 */
class UrlSuggestionModule
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    var enable = true

    private val database = DatabaseFinder().invoke(context)

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
        LayoutInflater.from(context),
        this::remove,
        ItemDeletionUseCase(bookmarkRepository, viewHistoryRepository)
    )

    private val queryUseCase: QueryUseCase

    private var binding: ViewCardUrlSuggestionBinding? = null

    init {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.view_card_url_suggestion,
            this,
            true
        )
        binding?.urlSuggestions?.adapter = adapter
        binding?.module = this
        binding?.urlSuggestions?.let { SwipeActionAttachment().invoke(it) }
        queryUseCase = QueryUseCase(
            adapter,
            bookmarkRepository,
            viewHistoryRepository,
            { if (it) show() else hide() }
        )
    }

    private fun remove(item: UrlItem) {
        adapter.remove(item)
    }

    fun openHistory() {
        (context as? FragmentActivity)?.let {
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
        if (!isVisible && enable) {
            runOnMainThread { isVisible = true }
        }
    }

    /**
     * Hide this module.
     */
    fun hide() {
        if (isVisible) {
            runOnMainThread { isVisible = false }
        }
    }
    
    fun setViewModel(viewModel: SearchFragmentViewModel) {
        adapter.setViewModel(viewModel)
    }

    private fun runOnMainThread(action: () -> Unit) =
            CoroutineScope(Dispatchers.Main).launch { action() }

}