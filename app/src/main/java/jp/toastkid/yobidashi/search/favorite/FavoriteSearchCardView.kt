package jp.toastkid.yobidashi.search.favorite

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.input.Inputs
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ViewCardFavoriteSearchBinding
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.search.SearchFragmentViewModel
import jp.toastkid.yobidashi.search.history.SwipeActionAttachment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Search history module.
 *
 * @author toastkidjp
 */
class FavoriteSearchCardView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    /**
     * RecyclerView's moduleAdapter.
     */
    private val moduleAdapter: ModuleAdapter

    /**
     * Database repository.
     */
    private val repository: FavoriteSearchRepository

    var enable: Boolean = true

    private var binding: ViewCardFavoriteSearchBinding? = null

    /**
     * Last subscription.
     */
    private var disposable: Job? = null

    init {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.view_card_favorite_search,
            this,
            true
        )
        binding?.module = this

        repository = DatabaseFinder().invoke(context).favoriteSearchRepository()

        binding?.searchFavorites?.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        moduleAdapter = ModuleAdapter(
                context,
                repository,
                { visible -> if (visible) { show() } else { hide() } },
                5
        )

        binding?.searchFavorites?.also {
            it.adapter = moduleAdapter
            it.onFlingListener = object : RecyclerView.OnFlingListener() {
                override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                    Inputs.hideKeyboard(this@FavoriteSearchCardView)
                    return false
                }
            }
            SwipeActionAttachment().invoke(it)
        }
    }

    fun openHistory() {
        (context as? FragmentActivity)?.let {
            ViewModelProvider(it).get(ContentViewModel::class.java)
                .nextFragment(FavoriteSearchFragment::class.java)
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
        moduleAdapter.setViewModel(viewModel)
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
