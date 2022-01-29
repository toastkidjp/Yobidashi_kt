package jp.toastkid.yobidashi.search.suggestion

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import jp.toastkid.api.suggestion.SuggestionApi
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ViewCardSearchSuggestionBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.network.NetworkChecker
import jp.toastkid.yobidashi.search.SearchFragmentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Facade of search suggestion module.
 * Initialize with binding object.
 *
 * @author toastkidjp
 */
class SuggestionCardView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    /**
     * Suggest ModuleAdapter.
     */
    private var adapter: Adapter? = null

    /**
     * Suggestion API.
     */
    private val suggestionApi = SuggestionApi()

    /**
     * Cache.
     */
    private val cache = HashMap<String, List<String>>(SUGGESTION_CACHE_CAPACITY)

    private var binding: ViewCardSearchSuggestionBinding? = null

    /**
     * Last subscription's lastSubscription.
     */
    private var lastSubscription: Job? = null

    init {
        val from = LayoutInflater.from(context)

        binding = DataBindingUtil.inflate(
            from,
            R.layout.view_card_search_suggestion,
            this,
            true
        )

        adapter = Adapter(from)

        val layoutManager = FlexboxLayoutManager(context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP
        layoutManager.justifyContent = JustifyContent.FLEX_START
        layoutManager.alignItems = AlignItems.STRETCH

        initializeSearchSuggestionList(layoutManager)
    }

    private fun initializeSearchSuggestionList(layoutManager: FlexboxLayoutManager) {
        binding?.searchSuggestions?.layoutManager = layoutManager
        binding?.searchSuggestions?.adapter = adapter
    }

    /**
     * Clear suggestion items.
     */
    fun clear() {
        adapter?.clear()
        adapter?.notifyDataSetChanged()
    }

    /**
     * Request web API.
     *
     * @param key
     */
    fun request(key: String) {
        lastSubscription?.cancel()

        if (cache.containsKey(key)) {
            val cachedList = cache[key] ?: return
            lastSubscription = replace(cachedList)
            return
        }

        val context = context
        if (NetworkChecker.isNotAvailable(context)) {
            return
        }

        if (PreferenceApplier(context).wifiOnly && NetworkChecker.isUnavailableWiFi(context)) {
            Toaster.tShort(context, R.string.message_wifi_not_connecting)
            return
        }

        suggestionApi.fetchAsync(key) { suggestions ->
            if (suggestions.isEmpty()) {
                CoroutineScope(Dispatchers.Main).launch { hide() }
                return@fetchAsync
            }
            cache[key] = suggestions
            lastSubscription = replace(suggestions)
        }
    }

    /**
     * Use for voice search.
     *
     * @param words Recognizer result words.
     */
    internal fun addAll(words: List<String>) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {
                words.forEach { adapter?.add(it) }
            }
            adapter?.notifyDataSetChanged()
        }
    }

    /**
     * Replace suggestions with specified items.
     *
     * @param suggestions
     * @return [Job]
     */
    private fun replace(suggestions: Iterable<String>): Job {
        return CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {
                adapter?.clear()
                suggestions.forEach { adapter?.add(it) }
            }
            show()
            adapter?.notifyDataSetChanged()
        }
    }

    /**
     * Show this module.
     */
    fun show() {
        if (!isVisible && isEnabled) {
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

    private fun runOnMainThread(action: () -> Unit) = post { action() }

    fun setViewModel(viewModel: SearchFragmentViewModel) {
        adapter?.setViewModel(viewModel)
    }

    /**
     * Dispose last subscription.
     */
    fun dispose() {
        lastSubscription?.cancel()
        binding = null
    }

    companion object {

        /**
         * Suggest cache capacity.
         */
        private const val SUGGESTION_CACHE_CAPACITY = 30
    }
}
