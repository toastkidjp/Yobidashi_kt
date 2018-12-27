package jp.toastkid.yobidashi.search.url_suggestion

import android.support.v7.widget.LinearLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.browser.history.ViewHistory_Relation
import jp.toastkid.yobidashi.databinding.ModuleUrlSuggestionBinding
import jp.toastkid.yobidashi.libs.db.DbInitter
import jp.toastkid.yobidashi.libs.facade.BaseModule
import jp.toastkid.yobidashi.libs.view.RightSwipeActionAttachment
import timber.log.Timber

/**
 * @author toastkidjp
 */
class UrlSuggestionModule(
        binding: ModuleUrlSuggestionBinding,
        browseCallback: (String) -> Unit,
        browseBackgroundCallback: (String) -> Unit
): BaseModule(binding.root) {

    /**
     * Adapter.
     */
    private val adapter = Adapter(
            binding.root.context,
            this::removeAt,
            browseCallback,
            browseBackgroundCallback
            )

    /**
     * Use for disposing.
     */
    private val disposables: CompositeDisposable = CompositeDisposable()

    /**
     * Database relation.
     */
    private val relation: ViewHistory_Relation = DbInitter.init(context()).relationOfViewHistory()

    init {
        binding.urlSuggestions.adapter = adapter
        binding.urlSuggestions.layoutManager =
                LinearLayoutManager(context(), LinearLayoutManager.VERTICAL, false)
        RightSwipeActionAttachment(
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
        adapter.removeAt(relation, index).addTo(disposables)
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

        return relation.selector()
                .where(relation.schema.url, "LIKE", "%$q%")
                .orderBy_idDesc()
                .limit(ITEM_LIMIT)
                .executeAsObservable()
                .subscribeOn(Schedulers.newThread())
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
     * Clear disposables.
     */
    fun dispose() {
        disposables.clear()
    }

    companion object {

        /**
         * Item limit.
         */
        private const val ITEM_LIMIT: Long = 3L
    }
}