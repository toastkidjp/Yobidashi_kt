package jp.toastkid.yobidashi.search.url_suggestion

import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import jp.toastkid.yobidashi.browser.history.ViewHistoryRepository
import jp.toastkid.yobidashi.databinding.ModuleUrlSuggestionBinding
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.libs.view.RightSwipeActionAttachment
import timber.log.Timber

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
            binding.root.context,
            this::removeAt,
            browseCallback,
            browseBackgroundCallback
            )

    var enable = true

    /**
     * Use for disposing.
     */
    private val disposables: CompositeDisposable = CompositeDisposable()

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
        RightSwipeActionAttachment()(
                binding.urlSuggestions,
                this::removeAt
        )
    }

    /**
     * Remove item.
     *
     * @param index
     */
    private fun removeAt(index: Int) {
        adapter.removeAt(viewHistoryRepository, index).addTo(disposables)
    }

    /**
     * Query to database.
     *
     * @param q query string
     */
    fun query(q: CharSequence): Disposable {
        adapter.clear()
        if (q.isEmpty()) {
            adapter.notifyDataSetChanged()
            hide()
            return Disposables.empty()
        }

        val bookmarkStream = Maybe.fromCallable { bookmarkRepository.search("%$q%", ITEM_LIMIT) }
                .subscribeOn(Schedulers.io())
                .flatMapObservable { it.toObservable() }

        val viewHistoryStream = Maybe.fromCallable { viewHistoryRepository.search("%$q%", ITEM_LIMIT) }
                .subscribeOn(Schedulers.io())
                .flatMapObservable { it.toObservable() }

        return Observable.concat(bookmarkStream, viewHistoryStream)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate {
                    if (adapter.isNotEmpty()) {
                        show()
                    } else {
                        hide()
                    }
                    adapter.notifyDataSetChanged()
                }
                .subscribe(adapter::add, Timber::e)
    }

    /**
     * Show this module.
     */
    fun show() {
        if (!binding.root.isVisible && enable) {
            runOnMainThread { binding.root.isVisible = true }
                    .addTo(disposables)
        }
    }

    /**
     * Hide this module.
     */
    fun hide() {
        if (binding.root.isVisible) {
            runOnMainThread { binding.root.isVisible = false }
                    .addTo(disposables)
        }
    }

    private fun runOnMainThread(action: () -> Unit) =
            Completable.fromAction { action() }
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {},
                            Timber::e
                    )

    /**
     * Clear disposables.
     */
    fun dispose() {
        disposables.clear()
    }

    companion object {

        /**
         * Item limit.
         */
        private const val ITEM_LIMIT = 3
    }
}