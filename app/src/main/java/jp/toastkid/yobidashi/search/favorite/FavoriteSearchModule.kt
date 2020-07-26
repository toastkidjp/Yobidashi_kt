package jp.toastkid.yobidashi.search.favorite

import android.os.Handler
import android.os.Looper
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.databinding.ModuleSearchFavoriteBinding
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.search.history.SwipeActionAttachment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Search history module.
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
) {

    /**
     * RecyclerView's moduleAdapter.
     */
    private val moduleAdapter: ModuleAdapter

    /**
     * Database repository.
     */
    private val repository: FavoriteSearchRepository

    var enable: Boolean = true

    /**
     * Last subscription.
     */
    private var disposable: Job? = null

    /**
     * For executing on UI thread.
     */
    private val uiThreadHandler = Handler(Looper.getMainLooper())

    init {

        binding.module = this

        val context = binding.root.context
        repository = DatabaseFinder().invoke(context).favoriteSearchRepository()

        binding.searchFavorites.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        moduleAdapter = ModuleAdapter(
                context,
                repository,
                searchCallback,
                { visible -> if (visible) { show() } else { hide() } },
                { history -> onClickAdd(history) },
                5
        )
        binding.searchFavorites.adapter = moduleAdapter
        binding.searchFavorites.onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                onTouch()
                return false
            }
        }
        uiThreadHandler.post {
            SwipeActionAttachment().invoke(binding.searchFavorites)
        }
    }

    /**
     * Query table with passed word.
     * @param s
     */
    fun query(s: CharSequence) {
        disposable?.cancel()
        disposable = moduleAdapter.query(s)
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

    /**
     * Dispose last subscription.
     */
    fun dispose() {
        disposable?.cancel()
    }

}
