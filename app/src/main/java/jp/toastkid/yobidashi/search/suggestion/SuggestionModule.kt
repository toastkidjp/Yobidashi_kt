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

    /** Suggest ModuleAdapter.  */
    private val mSuggestionAdapter: Adapter = Adapter(
            LayoutInflater.from(context()),
            searchInput,
            searchCallback,
            searchBackgroundCallback
    )

    /** Fetcher.  */
    private val mFetcher = SuggestionFetcher()

    /** Cache.  */
    private val mCache = HashMap<String, List<String>>(SUGGESTION_CACHE_CAPACITY)

    /** Last subscription's lastSubscription.  */
    private var lastSubscription: Disposable? = null

    /** Composite disposables. */
    private val disposables: CompositeDisposable = CompositeDisposable()

    init {
        binding.searchSuggestions.layoutManager = LinearLayoutManager(context())
        binding.searchSuggestions.adapter = mSuggestionAdapter
        binding.searchSuggestions.setOnTouchListener { _, _ ->
            onClick()
            false
        }
    }

    /**
     * Clear suggestion items.
     */
    fun clear() {
        mSuggestionAdapter.clear()
    }

    /**
     * Request web API.
     * @param key
     */
    fun request(key: String) {

        if (lastSubscription != null) {
            lastSubscription!!.dispose()
        }

        if (mCache.containsKey(key)) {
            lastSubscription = replace(mCache[key]!!).addTo(disposables)
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

        mFetcher.fetchAsync(key, { suggestions ->
            if (suggestions == null || suggestions.isEmpty()) {
                Completable.create { e ->
                    hide()
                    e.onComplete()
                }.subscribeOn(AndroidSchedulers.mainThread()).subscribe().addTo(disposables)
            } else {
                mCache.put(key, suggestions)
                lastSubscription = replace(suggestions).addTo(disposables)
            }
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
                .doOnTerminate { mSuggestionAdapter.notifyDataSetChanged() }
                .observeOn(Schedulers.computation())
                .subscribe({ mSuggestionAdapter.add(it) }, { Timber.e(it) })
                .addTo(disposables)
    }

    /**
     * Replace suggestions with specified items.
     *
     * @param suggestions
     */
    private fun replace(suggestions: List<String>): Disposable {

        return suggestions.toObservable()
                .doOnNext { mSuggestionAdapter.add(it) }
                .doOnSubscribe { d -> mSuggestionAdapter.clear() }
                .doOnTerminate {
                    show()
                    mSuggestionAdapter.notifyDataSetChanged()
                }
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    /**
     * Dispose last subscription.
     */
    fun dispose() {
        lastSubscription?.dispose()
        disposables.clear()
    }

    companion object {

        /** Suggest cache capacity.  */
        private val SUGGESTION_CACHE_CAPACITY = 30
    }
}
