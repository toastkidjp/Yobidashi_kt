package jp.toastkid.jitte.search.favorite

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import io.reactivex.disposables.Disposable
import jp.toastkid.jitte.databinding.ModuleSearchFavoriteBinding
import jp.toastkid.jitte.libs.db.Clear
import jp.toastkid.jitte.libs.db.DbInitter
import jp.toastkid.jitte.libs.facade.BaseModule

/**
 * Search hisotry module.

 * @author toastkidjp
 */
class FavoriteSearchModule
/**
 * Initialize with Data Binding object and so on...
 * @param binding
 * *
 * @param searchCallback
 * *
 * @param onTouch
 * *
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
                .invoke(Runnable {
                    moduleAdapter.clear()
                    hide()
                })
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
