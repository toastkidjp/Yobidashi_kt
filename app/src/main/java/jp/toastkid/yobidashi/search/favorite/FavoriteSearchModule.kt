package jp.toastkid.yobidashi.search.favorite

import android.os.Handler
import android.os.Looper
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import jp.toastkid.yobidashi.databinding.ModuleSearchFavoriteBinding
import jp.toastkid.yobidashi.libs.db.DbInitter
import jp.toastkid.yobidashi.libs.facade.BaseModule
import jp.toastkid.yobidashi.libs.view.RightSwipeActionAttacher

/**
 * Search hisotry module.
 *
 * @param binding Data binding object
 * @param searchCallback
 * @param onTouch
 * @param onClickAdd
 * @author toastkidjp
 */
class FavoriteSearchModule(
        private val binding: ModuleSearchFavoriteBinding,
        searchCallback: (FavoriteSearch) -> Unit,
        onTouch: () -> Unit,
        onClickAdd: (FavoriteSearch) -> Unit
) : BaseModule(binding.root) {

    /**
     * RecyclerView's moduleAdapter.
     */
    private val moduleAdapter: ModuleAdapter

    /**
     * Database relation.
     */
    private val relation: FavoriteSearch_Relation

    /**
     * Last subscription.
     */
    private var disposable: Disposable? = null

    /**
     * Use for disposing.
     */
    private val disposables: CompositeDisposable = CompositeDisposable()

    /**
     * For executing on UI thread.
     */
    private val uiThreadHandler = Handler(Looper.getMainLooper())

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
        uiThreadHandler.post {
            RightSwipeActionAttacher
                    .invoke(binding.searchFavorites, { moduleAdapter.removeAt(it).addTo(disposables) })
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
     * Confirm clear search history.
     */
    fun confirmClear() {
        val activityContext = context()
        if (activityContext is FragmentActivity) {
            DeleteAllFavoriteSearchDialogFragment().show(
                    activityContext.supportFragmentManager,
                    DeleteAllFavoriteSearchDialogFragment::class.java.simpleName
                    )
        }
    }

    fun clear() {
        moduleAdapter.clear()
        hide()
    }

    /**
     * Dispose last subscription.
     */
    fun dispose() {
        disposable?.dispose()
        disposables.clear()
    }

}
