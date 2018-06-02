package jp.toastkid.yobidashi.search

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.annotation.ColorInt
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v4.graphics.ColorUtils
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Spinner
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.*
import jp.toastkid.yobidashi.libs.Colors
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchActivity
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchModule
import jp.toastkid.yobidashi.search.history.HistoryModule
import jp.toastkid.yobidashi.search.history.SearchHistoryActivity
import jp.toastkid.yobidashi.search.suggestion.SuggestionModule
import jp.toastkid.yobidashi.search.url_suggestion.UrlSuggestionModule
import jp.toastkid.yobidashi.search.voice.VoiceSearch
import timber.log.Timber

/**
 * Search activity.
 *
 * @author toastkidjp
 */
class SearchActivity : BaseActivity() {

    /**
     * Disposables.
     */
    private val disposables: CompositeDisposable = CompositeDisposable()

    /**
     * View binder.
     */
    private var binding: ActivitySearchBinding? = null

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
     * Suggestion module.
     */
    private var urlSuggestionModule: UrlSuggestionModule? = null

    /**
     * Does use voice search?
     */
    private var useVoice: Boolean = true

    /**
     * Without exit animation flag.
     */
    private var withoutExitAnimation: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView(this, LAYOUT_ID)
        binding?.activity = this
        binding?.searchClear?.setOnClickListener ({ v -> binding?.searchInput?.setText("") })
        SearchCategorySpinnerInitializer.invoke(binding?.searchCategories as Spinner)

        initFavoriteModule()

        initHistoryModule()

        initSearchInput()

        applyColor()

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

        binding?.scroll?.setOnTouchListener({ v, event ->
            hideKeyboard()
            false
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding?.searchBar?.transitionName = "share"
        }

        binding?.toolbar?.let {
            initToolbar(it)
            it.setNavigationIcon(null)
            it.setPadding(0,0,0,0)
            it.setContentInsetsAbsolute(0,0)

            it.inflateMenu(R.menu.search_menu)
            it.menu.findItem(R.id.suggestion_check)?.isChecked = preferenceApplier.isEnableSuggestion
            it.menu.findItem(R.id.history_check)?.isChecked = preferenceApplier.isEnableSearchHistory

            // Set query.
            intent?.getStringExtra(EXTRA_KEY_QUERY)?.let {
                binding?.searchInput?.setText(it)
                overridePendingTransition(0, 0)
                withoutExitAnimation = true
            }
        }

        Toaster.snackShort(
                binding?.background as View,
                getString(R.string.message_search_on_background),
                colorPair()
                )
    }

    override fun clickMenu(item: MenuItem): Boolean {
        val itemId = item.itemId
        return when (itemId) {
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
            else -> super.clickMenu(item)
        }
    }

    private fun initFavoriteModule() {
        Completable.fromAction {
            favoriteModule = FavoriteSearchModule(
                    binding?.favoriteModule as ModuleSearchFavoriteBinding,
                    { fav -> search(fav.category as String, fav.query as String) },
                    this::hideKeyboard,
                    { fav ->
                        binding?.searchInput?.setText("${fav.query} ")
                        binding?.searchInput?.setSelection(
                                binding?.searchInput?.text.toString().length)
                    }
            )
        }
                .subscribeOn(Schedulers.newThread())
                .subscribe( { favoriteModule?.query("") }, { Timber.e(it) })
                .addTo(disposables)
    }

    /**
     * Initialize history module asynchronously.
     */
    private fun initHistoryModule() {
        Completable.fromAction {
            historyModule = HistoryModule(
                    binding?.historyModule as ModuleSearchHistoryBinding,
                    { history -> search(history.category as String, history.query as String) },
                    this::hideKeyboard,
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

        applyColorToToolbar(binding?.toolbar as Toolbar)

        suggestionModule?.enable = preferenceApplier.isEnableSuggestion
        historyModule?.enable = preferenceApplier.isEnableSearchHistory
        favoriteModule?.enable = preferenceApplier.isEnableFavoriteSearch
        urlSuggestionModule?.enable = preferenceApplier.isEnableViewHistory
    }

    /**
     * Initialize search input.
     */
    private fun initSearchInput() {
        binding?.searchInput?.let {
            it.setOnEditorActionListener({ v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search(binding?.searchCategories?.selectedItem.toString(), v.text.toString())
                }
                true
            })
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
     * TODO refactor
     *
     * @param useVoiceSearch
     */
    private fun setActionButtonState(useVoiceSearch: Boolean) {
        this.useVoice = useVoiceSearch
        if (useVoiceSearch) {
            binding?.searchAction?.setImageResource(R.drawable.ic_mic)
        } else {
            binding?.searchAction?.setImageResource(R.drawable.ic_search_white)
        }
    }

    /**
     * Apply color to views.
     */
    private fun applyColor() {
        val colorPair : ColorPair = colorPair()
        @ColorInt val bgColor:   Int = colorPair.bgColor()
        @ColorInt val fontColor: Int = colorPair.fontColor()
        Colors.setEditTextColor(binding?.searchInput as EditText, fontColor)

        binding?.searchActionBackground?.setBackgroundColor(ColorUtils.setAlphaComponent(bgColor, 128))
        binding?.searchAction?.setColorFilter(fontColor)
        binding?.searchAction?.setOnClickListener({ view ->
            if (useVoice) {
                startActivityForResult(VoiceSearch.makeIntent(this), VoiceSearch.REQUEST_CODE)
                return@setOnClickListener
            }
            search(
                    binding?.searchCategories?.selectedItem.toString(),
                    binding?.searchInput?.text.toString()
            )
        })
        binding?.searchClear?.setColorFilter(fontColor)
        binding?.searchInputBorder?.setBackgroundColor(fontColor)
    }

    /**
     * Open search result.
     *
     * @param category search category
     * @param query    search query
     * @param onBackground
     */
    private inline fun search(category: String, query: String, onBackground: Boolean = false) {
        SearchAction(this, category, query, onBackground)
                .invoke()
                .addTo(disposables)
        if (onBackground) {
            Toaster.snackShort(
                    binding?.root as View,
                    getString(R.string.message_background_search, query),
                    colorPair()
            )
        }
    }

    /**
     * Hide software keyboard.
     */
    fun hideKeyboard() {
        binding?.searchInput?.let { Inputs.hideKeyboard(it) }
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
    }

    override fun finish() {
        super.finish()
        if (withoutExitAnimation) {
            overridePendingTransition(0, 0)
        }
    }

    @StringRes
    override fun titleId(): Int = R.string.title_search

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.activity_search

        /**
         * Extra key.
         */
        private const val EXTRA_KEY_QUERY: String = "query"

        /**
         * Make launch [Intent].
         *
         * @param context [Context]
         */
        fun makeIntent(context: Context): Intent =
                Intent(context, SearchActivity::class.java)
                        .apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }

        /**
         * Make launcher [Intent] with query.
         *
         * @param context [Context]
         * @param query Query
         */
        fun makeIntentWithQuery(context: Context, query: String): Intent =
                makeIntent(context).apply { putExtra(EXTRA_KEY_QUERY, query) }

    }
}
