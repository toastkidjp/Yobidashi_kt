package jp.toastkid.yobidashi.search.history

import android.os.Handler
import android.os.Looper
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import jp.toastkid.yobidashi.databinding.ModuleSearchHistoryBinding
import jp.toastkid.yobidashi.libs.db.Clear
import jp.toastkid.yobidashi.libs.db.DbInitter
import jp.toastkid.yobidashi.libs.facade.BaseModule
import jp.toastkid.yobidashi.libs.view.RightSwipeActionAttacher

/**
 * Search hisotry module.
TODO Clean up code.
 * @author toastkidjp
 */
class HistoryModule
/**
 * Initialize with Data Binding object and so on...
 * @param binding
 *
 * @param searchCallback
 *
 * @param onTouch
 *
 * @param onClickAdd
 */
(
        /** Data binding object  */
        private val binding: ModuleSearchHistoryBinding,
        searchCallback: (SearchHistory) -> Unit,
        onTouch: () -> Unit,
        onClickAdd: (SearchHistory) -> Unit
) : BaseModule(binding.root) {

    /** RecyclerView's moduleAdapter.  */
    private val moduleAdapter: ModuleAdapter

    /** Database relation.  */
    private val relation: SearchHistory_Relation

    /** Last subscription.  */
    private var disposable: Disposable? = null

    /**
     * Use for disposing.
     */
    private val disposables: CompositeDisposable = CompositeDisposable()

    private val uiThreadhandler = Handler(Looper.getMainLooper())

    init {

        binding.module = this

        relation = DbInitter.init(context()).relationOfSearchHistory()

        binding.searchHistories.layoutManager = LinearLayoutManager(context(), LinearLayoutManager.VERTICAL, false)
        moduleAdapter = ModuleAdapter(
                context(),
                relation,
                searchCallback,
                { visible -> if (visible) { show() } else { hide() } },
                { history -> onClickAdd(history) }
        )
        binding.searchHistories.adapter = moduleAdapter
        binding.searchHistories.onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                onTouch()
                return false
            }
        }
        uiThreadhandler.post {
            RightSwipeActionAttacher
                    .invoke(binding.searchHistories, { moduleAdapter.removeAt(it).addTo(disposables) })
        }
    }

    /**
     * Query table with passed word.
     * @param s
     */
    fun query(s: CharSequence) {
        disposable?.dispose()
        disposable = moduleAdapter.query(s)
    }

    /**
     * Clear search history.
     * @param ignored
     */
    fun clearHistory(ignored: View) {
        Clear(binding.root, relation.deleter())
                .invoke{
                    moduleAdapter.clear()
                    hide()
                }
    }

    /**
     * Dispose last subscription.
     */
    fun dispose() {
        disposable?.dispose()
    }

}
