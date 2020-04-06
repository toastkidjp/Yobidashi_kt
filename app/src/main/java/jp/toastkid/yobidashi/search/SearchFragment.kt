/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.core.graphics.ColorUtils
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentSearchBinding
import jp.toastkid.yobidashi.databinding.ModuleHeaderSearchBinding
import jp.toastkid.yobidashi.databinding.ModuleSearchAppsBinding
import jp.toastkid.yobidashi.databinding.ModuleSearchFavoriteBinding
import jp.toastkid.yobidashi.databinding.ModuleSearchHistoryBinding
import jp.toastkid.yobidashi.databinding.ModuleSearchSuggestionBinding
import jp.toastkid.yobidashi.databinding.ModuleSearchUrlBinding
import jp.toastkid.yobidashi.databinding.ModuleUrlSuggestionBinding
import jp.toastkid.yobidashi.libs.EditTextColorSetter
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.network.NetworkChecker
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.main.HeaderViewModel
import jp.toastkid.yobidashi.main.content.ContentViewModel
import jp.toastkid.yobidashi.search.apps.AppModule
import jp.toastkid.yobidashi.search.category.SearchCategoryAdapter
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchFragment
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchModule
import jp.toastkid.yobidashi.search.history.HistoryModule
import jp.toastkid.yobidashi.search.history.SearchHistoryFragment
import jp.toastkid.yobidashi.search.suggestion.SuggestionModule
import jp.toastkid.yobidashi.search.url.UrlModule
import jp.toastkid.yobidashi.search.url_suggestion.UrlSuggestionModule
import jp.toastkid.yobidashi.search.voice.VoiceSearch
import timber.log.Timber

/**
 * @author toastkidjp
 */
class SearchFragment : Fragment() {

    /**
     * View binder.
     */
    private var binding: FragmentSearchBinding? = null

    /**
     * Favorite search module.
     */
    private var favoriteModule: FavoriteSearchModule? = null

    /**
     * History module.
     */
    private var historyModule: HistoryModule? = null

    /**
     * Suggestion module.
     */
    private var suggestionModule: SuggestionModule? = null

    /**
     * Current URL module.
     */
    private var urlModule: UrlModule? = null

    /**
     * Suggestion module.
     */
    private var urlSuggestionModule: UrlSuggestionModule? = null

    /**
     * App module.
     */
    private var appModule: AppModule? = null

    /**
     * Does use voice search?
     */
    private var useVoice: Boolean = true

    private lateinit var preferenceApplier: PreferenceApplier

    private var headerViewModel: HeaderViewModel? = null

    private var headerBinding: ModuleHeaderSearchBinding? = null

    private var currentTitle: String? = null

    private var currentUrl: String? = null

    /**
     * Disposables.
     */
    private val disposables: CompositeDisposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)

        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)

        headerBinding = DataBindingUtil.inflate(inflater, R.layout.module_header_search, container, false)
        headerBinding?.fragment = this
        headerBinding?.searchCategories?.let {
            it.adapter = SearchCategoryAdapter(context)
            val index = SearchCategory.findIndex(
                    SearchCategory.findByHostOrNull(currentUrl?.toUri()?.host)?.name
                            ?: PreferenceApplier(context).getDefaultSearchEngine()
            )
            it.setSelection(index)
        }

        currentTitle = arguments?.getString(EXTRA_KEY_TITLE)
        currentUrl = arguments?.getString(EXTRA_KEY_URL)

        preferenceApplier = PreferenceApplier(context)

        initFavoriteModule()

        initHistoryModule()

        initSearchInput()

        applyColor()

        urlModule = UrlModule(
                binding?.urlModule as ModuleSearchUrlBinding,
                this::setTextAndMoveCursorToEnd
        )

        suggestionModule = SuggestionModule(
                binding?.suggestionModule as ModuleSearchSuggestionBinding,
                headerBinding?.searchInput as EditText,
                { suggestion -> search(headerBinding?.searchCategories?.selectedItem.toString(), suggestion) },
                { suggestion -> search(headerBinding?.searchCategories?.selectedItem.toString(), suggestion, true) },
                this::hideKeyboard
        )

        urlSuggestionModule = UrlSuggestionModule(
                binding?.urlSuggestionModule as ModuleUrlSuggestionBinding,
                { suggestion -> search(headerBinding?.searchCategories?.selectedItem.toString(), suggestion) },
                { suggestion -> search(headerBinding?.searchCategories?.selectedItem.toString(), suggestion, true) }
        )

        appModule = AppModule(binding?.appModule as ModuleSearchAppsBinding)

        setListenerForKeyboardHiding()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            headerBinding?.searchBar?.transitionName = "share"
        }

        setQuery()

        activity?.also {
            headerViewModel = ViewModelProviders.of(it).get(HeaderViewModel::class.java)
        }

        /* TODO
        Toaster.snackShort(
                binding?.background as View,
                getString(R.string.message_search_on_background),
                preferenceApplier.colorPair()
        )*/

        setHasOptionsMenu(true)

        return binding?.root
    }

    fun clearInput() {
        headerBinding?.searchInput?.setText("")
    }

    private fun setQuery() {
        arguments?.getString(EXTRA_KEY_QUERY)?.let { query ->
            headerBinding?.searchInput?.let { input ->
                input.setText(query)
                input.selectAll()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListenerForKeyboardHiding() {
        binding?.scroll?.setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater?.inflate(R.menu.search_menu, menu)
        // TODO Move onResume
        menu?.findItem(R.id.suggestion_check)?.isChecked = preferenceApplier.isEnableSuggestion
        menu?.findItem(R.id.history_check)?.isChecked = preferenceApplier.isEnableSearchHistory
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId) {
        R.id.suggestion_check -> {
            preferenceApplier.switchEnableSuggestion()
            item.isChecked = preferenceApplier.isEnableSuggestion
            true
        }
        R.id.history_check -> {
            preferenceApplier.switchEnableSearchHistory()
            item.isChecked = preferenceApplier.isEnableSearchHistory
            true
        }
        R.id.open_favorite_search -> {
            activity?.also {
                ViewModelProviders.of(it)
                        .get(ContentViewModel::class.java)
                        .nextFragment(FavoriteSearchFragment::class.java)
            }
            true
        }
        R.id.open_search_history -> {
            activity?.also {
                ViewModelProviders.of(it)
                        .get(ContentViewModel::class.java)
                        .nextFragment(SearchHistoryFragment::class.java)
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    private fun initFavoriteModule() {
        Completable.fromAction {
            favoriteModule = FavoriteSearchModule(
                    binding?.favoriteModule as ModuleSearchFavoriteBinding,
                    { fav -> search(fav.category as String, fav.query as String) },
                    this::hideKeyboard,
                    { setTextAndMoveCursorToEnd("${it.query} ") }
            )
        }
                .subscribeOn(Schedulers.newThread())
                .subscribe( { favoriteModule?.query("") }, { Timber.e(it) })
                .addTo(disposables)
    }

    private fun setTextAndMoveCursorToEnd(text: String) {
        headerBinding?.searchInput?.setText(text)
        headerBinding?.searchInput?.setSelection(text.length)
    }

    /**
     * Initialize history module asynchronously.
     */
    @SuppressLint("SetTextI18n")
    private fun initHistoryModule() {
        Completable.fromAction {
            historyModule = HistoryModule(
                    binding?.historyModule as ModuleSearchHistoryBinding,
                    { history -> search(history.category as String, history.query as String) },
                    { searchHistory ->
                        headerBinding?.searchInput?.setText("${searchHistory.query} ")
                        headerBinding?.searchInput?.setSelection(
                                headerBinding?.searchInput?.text.toString().length)
                    }
            )
        }.subscribeOn(Schedulers.newThread())
                .subscribe(
                        { if (preferenceApplier.isEnableSearchHistory) { historyModule?.query("") } },
                        { Timber.e(it) }
                )
                .addTo(disposables)
    }

    override fun onResume() {
        super.onResume()
        activity?.let { Inputs.toggle(it) }

        suggestionModule?.enable = preferenceApplier.isEnableSuggestion
        historyModule?.enable = preferenceApplier.isEnableSearchHistory
        favoriteModule?.enable = preferenceApplier.isEnableFavoriteSearch
        urlModule?.enable = preferenceApplier.isEnableUrlModule()
        urlSuggestionModule?.enable = preferenceApplier.isEnableViewHistory
        appModule?.enable = preferenceApplier.isEnableAppSearch()

        val headerView = headerBinding?.root ?: return
        headerViewModel?.replace(headerView)
    }

    /**
     * Initialize search input.
     */
    private fun initSearchInput() {
        headerBinding?.searchInput?.let {
            it.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search(headerBinding?.searchCategories?.selectedItem.toString(), v.text.toString())
                }
                true
            }
            it.addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    val key = s.toString()

                    (if (key.isEmpty()|| key == currentUrl) urlModule?.switch(currentTitle, currentUrl)
                    else urlModule?.hide())
                            ?.addTo(disposables)

                    setActionButtonState(key.isEmpty())

                    if (preferenceApplier.isEnableSearchHistory) {
                        historyModule?.query(s)
                    }
                    favoriteModule?.query(s)
                    urlSuggestionModule?.query(s)

                    if (preferenceApplier.isEnableAppSearch()) {
                        appModule?.request(key)
                    } else {
                        appModule?.hide()
                    }

                    if (preferenceApplier.isDisableSuggestion) {
                        suggestionModule?.clear()
                        return
                    }

                    suggestionModule?.request(key)
                }

                override fun afterTextChanged(s: Editable) = Unit
            })
        }
    }

    /**
     * Set action button state.
     *
     * @param useVoiceSearch
     */
    private fun setActionButtonState(useVoiceSearch: Boolean) {
        this.useVoice = useVoiceSearch
        (if (useVoiceSearch) R.drawable.ic_mic else R.drawable.ic_search_white)
                .also { headerBinding?.searchAction?.setImageResource(it) }
    }

    /**
     * Apply color to views.
     */
    private fun applyColor() {
        val colorPair : ColorPair = preferenceApplier.colorPair()
        @ColorInt val bgColor:   Int = colorPair.bgColor()
        @ColorInt val fontColor: Int = colorPair.fontColor()
        EditTextColorSetter().invoke(headerBinding?.searchInput, fontColor)

        headerBinding?.also {
            it.searchActionBackground.setBackgroundColor(ColorUtils.setAlphaComponent(bgColor, 128))
            it.searchAction.setColorFilter(fontColor)
            it.searchClear.setColorFilter(fontColor)
            it.searchInputBorder.setBackgroundColor(fontColor)
        }
    }

    fun invokeSearch() {
        if (useVoice) {
            try {
                startActivityForResult(VoiceSearch.makeIntent(requireContext()), VoiceSearch.REQUEST_CODE)
            } catch (e: ActivityNotFoundException) {
                Timber.e(e)
                VoiceSearch.suggestInstallGoogleApp(binding?.root as View, preferenceApplier.colorPair())
            }
            return
        }
        search(
                headerBinding?.searchCategories?.selectedItem.toString(),
                headerBinding?.searchInput?.text.toString()
        )
    }

    /**
     * Open search result.
     *
     * @param category search category
     * @param query    search query
     * @param onBackground
     */
    @Suppress("NOTHING_TO_INLINE")
    private inline fun search(category: String, query: String, onBackground: Boolean = false) {
        val context = requireContext()
        if (NetworkChecker.isNotAvailable(context)) {
            Toaster.snackShort(
                    binding?.root as View,
                    "Network is not available...",
                    preferenceApplier.colorPair()
            )
            return
        }
        SearchAction(context, category, query, currentUrl, onBackground)
                .invoke()
                .addTo(disposables)
        if (onBackground) {
            Toaster.snackShort(
                    binding?.root as View,
                    getString(R.string.message_background_search, query),
                    preferenceApplier.colorPair()
            )
        }
    }

    /**
     * Hide software keyboard.
     */
    private fun hideKeyboard() {
        headerBinding?.searchInput?.let { Inputs.hideKeyboard(it) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            VoiceSearch.REQUEST_CODE -> {
                suggestionModule?.run {
                    show()
                    val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (result?.size == 0) {
                        return
                    }
                    clear()
                    addAll(result ?: emptyList())
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    override fun onDetach() {
        disposables.clear()
        favoriteModule?.dispose()
        historyModule?.dispose()
        suggestionModule?.dispose()
        urlSuggestionModule?.dispose()
        appModule?.dispose()
        super.onDetach()
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID = R.layout.fragment_search


        /**
         * Extra key of query.
         */
        private const val EXTRA_KEY_QUERY = "query"

        /**
         * Extra key of title.
         */
        private const val EXTRA_KEY_TITLE = "title"

        /**
         * Extra key of URL.
         */
        private const val EXTRA_KEY_URL = "url"

        /**
         * Make launch [Intent].
         *
         * @param context [Context]
         */
        fun makeWith(context: Context, title: String? = null, url: String? = null) =
                SearchFragment()
                        .also { fragment ->
                            fragment.arguments = Bundle().also { bundle ->
                                title?.let {
                                    bundle.putString(EXTRA_KEY_TITLE, it)
                                }
                                url?.let {
                                    bundle.putString(EXTRA_KEY_URL, it)
                                }
                            }
                        }

        /**
         * Make launcher [Intent] with query.
         *
         * @param context [Context]
         * @param query Query
         * @param title Title
         * @param url URL
         */
        fun makeWithQuery(context: Context, query: String, title: String?, url: String? = null) =
                makeWith(context, title, url).also { fragment ->
                    fragment.arguments?.putString(EXTRA_KEY_QUERY, query)
                }

    }
}