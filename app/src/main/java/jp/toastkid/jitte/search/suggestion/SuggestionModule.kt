package jp.toastkid.jitte.search.suggestion

import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText

import java.util.HashMap

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import jp.toastkid.jitte.databinding.ModuleSearchSuggestionBinding
import jp.toastkid.jitte.libs.facade.BaseModule
import jp.toastkid.jitte.libs.network.NetworkChecker

/**
 * Facade of search suggestion module.

 * @author toastkidjp
 */
class SuggestionModule
/**
 * Initialize with binding object.

 * @param binding
 * *
 * @param searchInput
 */
(
        binding: ModuleSearchSuggestionBinding,
        searchInput: EditText,
        searchCallback: Consumer<String>,
        onClick: Runnable
) : BaseModule(binding.root) {

    /** Suggest ModuleAdapter.  */
    private val mSuggestionAdapter: Adapter

    /** Fetcher.  */
    private val mFetcher = SuggestionFetcher()

    /** Cache.  */
    private val mCache = HashMap<String, List<String>>(SUGGESTION_CACHE_CAPACITY)

    /** Last subscription's disposable.  */
    private var disposable: Disposable? = null

    init {
        mSuggestionAdapter = Adapter(
                LayoutInflater.from(context()),
                searchInput,
                Consumer<String>{ suggestion -> searchCallback.accept(suggestion) }
        )
        binding.searchSuggestions.layoutManager = LinearLayoutManager(context())
        binding.searchSuggestions.adapter = mSuggestionAdapter
        binding.searchSuggestions.setOnTouchListener (View.OnTouchListener{ v, event ->
            onClick.run()
            false
        })
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

        if (disposable != null) {
            disposable!!.dispose()
        }

        if (mCache.containsKey(key)) {
            disposable = replace(mCache[key]!!)
            return
        }

        if (NetworkChecker.isNotAvailable(context())) {
            return
        }

        mFetcher.fetchAsync(key, Consumer<List<String>>{ suggestions ->
            if (suggestions == null || suggestions.isEmpty()) {
                Completable.create { e ->
                    hide()
                    e.onComplete()
                }.subscribeOn(AndroidSchedulers.mainThread()).subscribe()
                return@Consumer
            }
            mCache.put(key, suggestions)
            disposable = replace(suggestions)
        })
    }

    /**
     * Replace suggestions with specified items.

     * @param suggestions
     */
    private fun replace(suggestions: List<String>): Disposable {

        return Observable.fromIterable(suggestions)
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
        if (disposable != null) {
            disposable!!.dispose()
        }
    }

    companion object {

        /** Suggest cache capacity.  */
        private val SUGGESTION_CACHE_CAPACITY = 30
    }
}
