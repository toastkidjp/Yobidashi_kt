package jp.toastkid.yobidashi.search.favorite

import android.os.Handler
import android.os.Looper
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import jp.toastkid.yobidashi.databinding.ModuleSearchFavoriteBinding
import jp.toastkid.yobidashi.libs.db.Clear
import jp.toastkid.yobidashi.libs.db.DbInitter
import jp.toastkid.yobidashi.libs.facade.BaseModule
import jp.toastkid.yobidashi.libs.view.RightSwipeActionAttacher

/**
 * Search hisotry module.
TODO clean up code.
 * @author toastkidjp
 */
class FavoriteSearchModule
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
        private val binding: ModuleSearchFavoriteBinding,
        searchCallback: (FavoriteSearch) -> Unit,
        onTouch: () -> Unit,
        onClickAdd: (FavoriteSearch) -> Unit
) : BaseModule(binding.root) {

    /** RecyclerView's moduleAdapter.  */
    private val moduleAdapter: ModuleAdapter

    /** Database relation.  */
    private val relation: FavoriteSearch_Relation

    /** Last subscription.  */
    private var disposable: Disposable? = null

    /**
     * Use for disposing.
     */
    private val disposables: CompositeDisposable = CompositeDisposable()

    private val uiThreadhandler = Handler(Looper.getMainLooper())

    init {

        binding.module = this

        relation = DbInitter.init(context()).relationOfFavoriteSearch()

        binding.searchFavorites.layoutManager = LinearLayoutManager(context(), LinearLayoutManager.VERTICAL, false)
        moduleAdapter = ModuleAdapter(
                context(),
                relation,
                searchCallback,
                { visible -> if (visible) { show() } else { hide() } },
                { history -> onClickAdd(history) }
        )
        binding.searchFavorites.adapter = moduleAdapter
        binding.searchFavorites.onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                onTouch()
                return false
            }
        }
        uiThreadhandler.post {
            RightSwipeActionAttacher
                    .invoke(binding.searchFavorites, { disposables.add(moduleAdapter.removeAt(it)) })
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
    fun clearHistory(ignored: View) {
        Clear(binding.root, relation.deleter())
                .invoke {
                    moduleAdapter.clear()
                    hide()
                }
    }

    /**
     * Dispose last subscription.
     */
    fun dispose() {
        disposable?.dispose()
        disposables.clear()
    }

}
