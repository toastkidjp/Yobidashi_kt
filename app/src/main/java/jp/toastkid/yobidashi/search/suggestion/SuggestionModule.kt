package jp.toastkid.yobidashi.search.suggestion

import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.widget.EditText
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModuleSearchSuggestionBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.WifiConnectionChecker
import jp.toastkid.yobidashi.libs.facade.BaseModule
import jp.toastkid.yobidashi.libs.network.NetworkChecker
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import timber.log.Timber
import java.util.*

/**
 * Facade of search suggestion module.
 * Initialize with binding object.
 *
 * @param binding
 * @param searchInput
 *
 * @author toastkidjp
 */
class SuggestionModule(
        binding: ModuleSearchSuggestionBinding,
        searchInput: EditText,
        searchCallback: (String) -> Unit,
        searchBackgroundCallback: (String) -> Unit,
        onClick: () -> Unit
) : BaseModule(binding.root) {

    /**
     * Suggest ModuleAdapter.
     */
    private val adapter: Adapter = Adapter(
            LayoutInflater.from(context()),
            searchInput,
            searchCallback,
            searchBackgroundCallback
    )

    /**
     * Fetcher.
     */
    private val fetcher = SuggestionFetcher()

    /**
     * Cache.
     */
    private val cache = HashMap<String, List<String>>(SUGGESTION_CACHE_CAPACITY)

    /**
     * Last subscription's lastSubscription.
     */
    private var lastSubscription: Disposable? = null

    /**
     * Composite disposables.
     */
    private val disposables: CompositeDisposable = CompositeDisposable()

    init {
        binding.searchSuggestions.layoutManager = LinearLayoutManager(context())
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
     * @param key
     */
    fun request(key: String) {
        lastSubscription?.dispose()

        if (cache.containsKey(key)) {
            lastSubscription = replace(cache[key]!!).addTo(disposables)
            return
        }

        val context = context()
        if (NetworkChecker.isNotAvailable(context)) {
            return
        }

        if (PreferenceApplier(context).wifiOnly && WifiConnectionChecker.isNotConnecting(context)) {
            Toaster.tShort(context, R.string.message_wifi_not_connecting)
            return
        }

        fetcher.fetchAsync(key, { suggestions ->
            if (suggestions.isEmpty()) {
                Completable.fromAction { hide() }
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe({}, Timber::e)
                        .addTo(disposables)
                return@fetchAsync
            }
            cache.put(key, suggestions)
            lastSubscription = replace(suggestions).addTo(disposables)
        })
    }

    /**
     * Use for voice search.
     *
     * @param words
     */
    internal fun addAll(words: List<String>) {
        words.toObservable()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate { adapter.notifyDataSetChanged() }
                .observeOn(Schedulers.computation())
                .subscribe(adapter::add, Timber::e)
                .addTo(disposables)
    }

    /**
     * Replace suggestions with specified items.
     *
     * @param suggestions
     */
    private fun replace(suggestions: Iterable<String>): Disposable =
            suggestions.toObservable()
                    .doOnNext { adapter.add(it) }
                    .doOnSubscribe { d -> adapter.clear() }
                    .doOnTerminate {
                        show()
                        adapter.notifyDataSetChanged()
                    }
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe({}, Timber::e)
                    .addTo(disposables)

    /**
     * Dispose last subscription.
     */
    fun dispose() {
        lastSubscription?.dispose()
        disposables.clear()
    }

    companion object {

        /**
         * Suggest cache capacity.
         */
        private val SUGGESTION_CACHE_CAPACITY = 30
    }
}
