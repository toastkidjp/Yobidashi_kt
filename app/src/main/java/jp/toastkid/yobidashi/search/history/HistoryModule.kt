package jp.toastkid.yobidashi.search.history

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import io.reactivex.disposables.Disposable
import jp.toastkid.yobidashi.databinding.ModuleSearchHistoryBinding
import jp.toastkid.yobidashi.libs.db.Clear
import jp.toastkid.yobidashi.libs.db.DbInitter
import jp.toastkid.yobidashi.libs.facade.BaseModule

/**
 * Search hisotry module.

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
    }

    /**
     * Query table with passed word.
     * @param s
     */
    fun query(s: CharSequence) {
        if (disposable != null) {
            disposable!!.dispose()
        }
        disposable = moduleAdapter.query(s)
    }

    /**
     * Clear search history.
     * @param view
     */
    fun clearHistory(view: View) {
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
        if (disposable != null) {
            disposable!!.dispose()
        }
    }

}
