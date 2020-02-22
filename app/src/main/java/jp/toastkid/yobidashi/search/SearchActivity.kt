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
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.ColorUtils
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.*
import jp.toastkid.yobidashi.libs.EditTextColorSetter
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.libs.network.NetworkChecker
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.view.ToolbarColorApplier
import jp.toastkid.yobidashi.main.MainActivity
import jp.toastkid.yobidashi.search.apps.AppModule
import jp.toastkid.yobidashi.search.clip.ClipboardModule
import jp.toastkid.yobidashi.search.favorite.ClearFavoriteSearchDialogFragment
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchActivity
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchModule
import jp.toastkid.yobidashi.search.history.ClearSearchHistoryDialogFragment
import jp.toastkid.yobidashi.search.history.HistoryModule
import jp.toastkid.yobidashi.search.history.SearchHistoryActivity
import jp.toastkid.yobidashi.search.suggestion.SuggestionModule
import jp.toastkid.yobidashi.search.url.UrlModule
import jp.toastkid.yobidashi.search.url_suggestion.UrlSuggestionModule
import jp.toastkid.yobidashi.search.voice.VoiceSearch
import jp.toastkid.yobidashi.settings.SettingsActivity
import timber.log.Timber

/**
 * Search activity.
 *
 * @author toastkidjp
 */
class SearchActivity : AppCompatActivity(),
        ClearFavoriteSearchDialogFragment.Callback,
        ClearSearchHistoryDialogFragment.Callback
{

    /**
     * Disposables.
     */
    private val disposables: CompositeDisposable = CompositeDisposable()

    /**
     * View binder.
     */
    private var binding: ActivitySearchBinding? = null

    private var clipboardModule: ClipboardModule? = null

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

    /**
     * Without exit animation flag.
     */
    private var withoutExitAnimation: Boolean = false

    private lateinit var preferenceApplier: PreferenceApplier

    private var currentTitle: String? = null

    private var currentUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)

        currentTitle = intent.getStringExtra(EXTRA_KEY_TITLE)
        currentUrl = intent.getStringExtra(EXTRA_KEY_URL)

        preferenceApplier = PreferenceApplier(this)

        binding = DataBindingUtil.setContentView(this, LAYOUT_ID)
        binding?.activity = this
        binding?.searchClear?.setOnClickListener { binding?.searchInput?.setText("") }
        binding?.searchCategories?.let {
            SearchCategorySpinnerInitializer.invoke(
                    it,
                    SearchCategory.findByHostOrNull(currentUrl?.toUri()?.host)
            )
        }

        initFavoriteModule()

        initHistoryModule()

        initSearchInput()

        applyColor()

        clipboardModule = ClipboardModule(binding?.clipboardModule as ModuleSearchClipboardBinding)
        { clipped ->
            if (Urls.isValidUrl(clipped)) {
                finish()
                startActivity(MainActivity.makeBrowserIntent(this, clipped.toUri()))
            } else {
                search(binding?.searchCategories?.selectedItem.toString(), clipped)
            }
        }

        urlModule = UrlModule(
                binding?.urlModule as ModuleSearchUrlBinding,
                this::setTextAndMoveCursorToEnd
        )

        suggestionModule = SuggestionModule(
                binding?.suggestionModule as ModuleSearchSuggestionBinding,
                binding?.searchInput as EditText,
                { suggestion -> search(binding?.searchCategories?.selectedItem.toString(), suggestion) },
                { suggestion -> search(binding?.searchCategories?.selectedItem.toString(), suggestion, true) },
                this::hideKeyboard
        )

        urlSuggestionModule = UrlSuggestionModule(
                binding?.urlSuggestionModule as ModuleUrlSuggestionBinding,
                { suggestion -> search(binding?.searchCategories?.selectedItem.toString(), suggestion) },
                { suggestion -> search(binding?.searchCategories?.selectedItem.toString(), suggestion, true) }
        )

        appModule = AppModule(binding?.appModule as ModuleSearchAppsBinding)

        setListenerForKeyboardHiding()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding?.searchBar?.transitionName = "share"
        }

        binding?.toolbar?.also { toolbar ->
            toolbar.navigationIcon = null
            toolbar.setPadding(0,0,0,0)
            toolbar.setContentInsetsAbsolute(0,0)

            toolbar.inflateMenu(R.menu.settings_toolbar_menu)
            toolbar.inflateMenu(R.menu.search_menu)
            toolbar.menu.findItem(R.id.suggestion_check)?.isChecked = preferenceApplier.isEnableSuggestion
            toolbar.menu.findItem(R.id.history_check)?.isChecked = preferenceApplier.isEnableSearchHistory
            toolbar.setOnMenuItemClickListener { clickMenu(it) }

            setQuery()
        }

        Toaster.snackShort(
                binding?.background as View,
                getString(R.string.message_search_on_background),
                preferenceApplier.colorPair()
                )
    }

    private fun setQuery() {
        intent?.getStringExtra(EXTRA_KEY_QUERY)?.let { query ->
            binding?.searchInput?.let { input ->
                input.setText(query)
                input.selectAll()
            }
            overridePendingTransition(0, 0)
            withoutExitAnimation = true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListenerForKeyboardHiding() {
        binding?.scroll?.setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }
    }

    private fun clickMenu(item: MenuItem): Boolean = when (item.itemId) {
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
            startActivity(FavoriteSearchActivity.makeIntent(this))
            true
        }
        R.id.open_search_history -> {
            startActivity(SearchHistoryActivity.makeIntent(this))
            true
        }
        R.id.setting -> {
            startActivity(SettingsActivity.makeIntent(this))
            true
        }
        R.id.menu_close -> {
            finish()
            true
        }
        R.id.menu_exit -> {
            moveTaskToBack(true)
            true
        }
        else -> true
    }

    @SuppressLint("SetTextI18n")
    private fun initFavoriteModule() {
        Completable.fromAction {
            favoriteModule = FavoriteSearchModule(
                    binding?.favoriteModule as ModuleSearchFavoriteBinding,
                    { fav -> search(fav.category as String, fav.query as String) },
                    this::hideKeyboard,
                    { fav -> // TODO Clean it.
                        setTextAndMoveCursorToEnd("${fav.query} ")
                    }
            )
        }
                .subscribeOn(Schedulers.newThread())
                .subscribe( { favoriteModule?.query("") }, { Timber.e(it) })
                .addTo(disposables)
    }

    private fun setTextAndMoveCursorToEnd(text: String) {
        binding?.searchInput?.setText(text)
        binding?.searchInput?.setSelection(text.length)
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
                        binding?.searchInput?.setText("${searchHistory.query} ")
                        binding?.searchInput?.setSelection(
                                binding?.searchInput?.text.toString().length)
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
        Inputs.toggle(this)

        ToolbarColorApplier()(window, binding?.toolbar as Toolbar, preferenceApplier.colorPair())

        suggestionModule?.enable = preferenceApplier.isEnableSuggestion
        historyModule?.enable = preferenceApplier.isEnableSearchHistory
        favoriteModule?.enable = preferenceApplier.isEnableFavoriteSearch
        urlModule?.enable = preferenceApplier.isEnableUrlModule()
        urlSuggestionModule?.enable = preferenceApplier.isEnableViewHistory
        appModule?.enable = preferenceApplier.isEnableAppSearch()

        urlModule?.switch(currentTitle, currentUrl)?.addTo(disposables)
    }

    /**
     * For Android 10 and later.
     *
     * @param hasFocus
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        clipboardModule?.switch()
    }

    /**
     * Initialize search input.
     */
    private fun initSearchInput() {
        binding?.searchInput?.let {
            it.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search(binding?.searchCategories?.selectedItem.toString(), v.text.toString())
                }
                true
            }
            it.addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    val key = s.toString()

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
                .also { binding?.searchAction?.setImageResource(it) }
    }

    /**
     * Apply color to views.
     */
    private fun applyColor() {
        val colorPair : ColorPair = preferenceApplier.colorPair()
        @ColorInt val bgColor:   Int = colorPair.bgColor()
        @ColorInt val fontColor: Int = colorPair.fontColor()
        EditTextColorSetter().invoke(binding?.searchInput, fontColor)

        binding?.also {
            it.searchActionBackground.setBackgroundColor(ColorUtils.setAlphaComponent(bgColor, 128))
            it.searchAction.setColorFilter(fontColor)
            it.searchAction.setOnClickListener {
                if (useVoice) {
                    try {
                        startActivityForResult(VoiceSearch.makeIntent(this), VoiceSearch.REQUEST_CODE)
                    } catch (e: ActivityNotFoundException) {
                        Timber.e(e)
                        VoiceSearch.suggestInstallGoogleApp(binding?.root as View, colorPair)
                    }
                    return@setOnClickListener
                }
                search(
                        binding?.searchCategories?.selectedItem.toString(),
                        binding?.searchInput?.text.toString()
                )
            }
            it.searchClear.setColorFilter(fontColor)
            it.searchInputBorder.setBackgroundColor(fontColor)
        }
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
        if (NetworkChecker.isNotAvailable(this)) {
            Toaster.snackShort(
                    binding?.root as View,
                    "Network is not available...",
                    preferenceApplier.colorPair()
            )
            return
        }
        SearchAction(this, category, query, currentUrl, onBackground)
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
        binding?.searchInput?.let { Inputs.hideKeyboard(it) }
    }

    override fun onClickDeleteAllFavoriteSearch() {
        val repository = DatabaseFinder().invoke(this).favoriteSearchRepository()
        Completable.fromAction { repository.deleteAll() }
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {
                            favoriteModule?.clear()
                            Toaster.snackShort(
                                    binding?.root as View,
                                    R.string.settings_color_delete,
                                    PreferenceApplier(this).colorPair()
                            )
                        },
                        Timber::e
                )
                .addTo(disposables)
    }

    override fun onClickClearSearchHistory() {
        Completable.fromAction { DatabaseFinder().invoke(this).searchHistoryRepository().deleteAll() }
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {
                            historyModule?.clear()
                            Toaster.snackShort(
                                    binding?.root as View,
                                    R.string.settings_color_delete,
                                    PreferenceApplier(this).colorPair()
                            )
                        },
                        Timber::e
                )
                .addTo(disposables)
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

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
        favoriteModule?.dispose()
        historyModule?.dispose()
        suggestionModule?.dispose()
        urlSuggestionModule?.dispose()
        clipboardModule?.dispose()
        appModule?.dispose()
    }

    override fun finish() {
        super.finish()
        if (withoutExitAnimation) {
            overridePendingTransition(0, 0)
        }
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID = R.layout.activity_search

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
        fun makeIntent(context: Context, title: String? = null, url: String? = null) =
                Intent(context, SearchActivity::class.java)
                        .apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            title?.let {
                                putExtra(EXTRA_KEY_TITLE, it)
                            }
                            url?.let {
                                putExtra(EXTRA_KEY_URL, it)
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
        fun makeIntentWithQuery(context: Context, query: String, title: String?, url: String? = null): Intent =
                makeIntent(context, title, url).apply { putExtra(EXTRA_KEY_QUERY, query) }

    }
}
