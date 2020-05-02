package jp.toastkid.yobidashi.search.suggestion

import android.view.LayoutInflater
import android.widget.EditText
import androidx.core.view.isVisible
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModuleSearchSuggestionBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.network.NetworkChecker
import jp.toastkid.yobidashi.libs.network.WifiConnectionChecker
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

/**
 * Facade of search suggestion module.
 * Initialize with binding object.
 *
 * @param binding Data binding object
 * @param searchInput Input field
 * @param searchCallback Callback on search
 * @param searchBackgroundCallback Callback for background search
 * @param onClick Callback on click
 *
 * @author toastkidjp
 */
class SuggestionModule(
        private val binding: ModuleSearchSuggestionBinding,
        searchInput: EditText,
        searchCallback: (String) -> Unit,
        searchBackgroundCallback: (String) -> Unit,
        onClick: () -> Unit
) {

    /**
     * Suggest ModuleAdapter.
     */
    private val adapter: Adapter = Adapter(
            LayoutInflater.from(binding.root.context),
            searchInput,
            searchCallback,
            searchBackgroundCallback
    )

    /**
     * Suggestion API.
     */
    private val suggestionApi = SuggestionApi()

    /**
     * Cache.
     */
    private val cache = HashMap<String, List<String>>(SUGGESTION_CACHE_CAPACITY)

    /**
     * Last subscription's lastSubscription.
     */
    private var lastSubscription: Job? = null

    var enable: Boolean = true

    /**
     * Composite disposables.
     */
    private val disposables: CompositeDisposable = CompositeDisposable()

    init {
        val layoutManager = FlexboxLayoutManager(binding.root.context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP
        layoutManager.justifyContent = JustifyContent.FLEX_START
        layoutManager.alignItems = AlignItems.STRETCH

        binding.searchSuggestions.layoutManager = layoutManager
        binding.searchSuggestions.adapter = adapter
        binding.searchSuggestions.setOnTouchListener { _, _ ->
            onClick()
            false
        }
    }

    /**
     * Clear suggestion items.
     */
    fun clear() {
        adapter.clear()
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

        val context = binding.root.context
        if (NetworkChecker.isNotAvailable(context)) {
            return
        }

        if (PreferenceApplier(context).wifiOnly && WifiConnectionChecker.isNotConnecting(context)) {
            Toaster.tShort(context, R.string.message_wifi_not_connecting)
            return
        }

        suggestionApi.fetchAsync(key) { suggestions ->
            if (suggestions.isEmpty()) {
                Completable.fromAction { hide() }
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe({}, Timber::e)
                        .addTo(disposables)
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
                words.forEach { adapter.add(it) }
            }
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * Replace suggestions with specified items.
     *
     * @param suggestions
     * @return [Disposable]
     */
    private fun replace(suggestions: Iterable<String>): Job {
        return CoroutineScope(Dispatchers.Main).launch {
            adapter.clear()// TODO consider to implement replace to adapter.
            withContext(Dispatchers.Default) {
                suggestions.forEach { adapter.add(it) }
            }
            show()
            adapter.notifyDataSetChanged()
        }
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
     * Dispose last subscription.
     */
    fun dispose() {
        lastSubscription?.cancel()
        disposables.clear()
    }

    companion object {

        /**
         * Suggest cache capacity.
         */
        private const val SUGGESTION_CACHE_CAPACITY = 30
    }
}
