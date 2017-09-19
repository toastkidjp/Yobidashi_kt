package jp.toastkid.yobidashi.search

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.annotation.ColorInt
import android.support.v4.graphics.ColorUtils
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivitySearchBinding
import jp.toastkid.yobidashi.databinding.ModuleSearchFavoriteBinding
import jp.toastkid.yobidashi.databinding.ModuleSearchHistoryBinding
import jp.toastkid.yobidashi.databinding.ModuleSearchSuggestionBinding
import jp.toastkid.yobidashi.libs.Colors
import jp.toastkid.yobidashi.libs.ImageLoader
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchActivity
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchModule
import jp.toastkid.yobidashi.search.history.HistoryModule
import jp.toastkid.yobidashi.search.history.SearchHistoryActivity
import jp.toastkid.yobidashi.search.suggestion.SuggestionModule
import jp.toastkid.yobidashi.search.voice.VoiceSearch

/**
 * Search activity.
 *
 * @author toastkidjp
 */
class SearchActivity : BaseActivity() {

    /** View binder.  */
    private var binding: ActivitySearchBinding? = null

    /** Disposables.  */
    private val disposables: CompositeDisposable = CompositeDisposable()

    /** Favorite search module. */
    private lateinit var favoriteModule: FavoriteSearchModule

    /** History module.  */
    private lateinit var historyModule: HistoryModule

    /** Suggestion module.  */
    private lateinit var suggestionModule: SuggestionModule

    /** Does use voice search? */
    private var useVoice: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView(this, LAYOUT_ID)
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
                this::hideKeyboard
        )

        binding?.scroll?.setOnTouchListener({ v, event ->
            hideKeyboard()
            false
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding?.searchBar?.transitionName = "share"
        }

        initToolbar(binding?.toolbar as Toolbar)
        binding?.toolbar?.setNavigationIcon(null)
        binding?.toolbar?.setPadding(0,0,0,0);
        binding?.toolbar?.setContentInsetsAbsolute(0,0);
        binding?.toolbar?.inflateMenu(R.menu.search_menu)
        val menu = binding?.toolbar?.menu
        menu?.findItem(R.id.suggestion_check)?.isChecked = preferenceApplier.isEnableSuggestion
        menu?.findItem(R.id.history_check)?.isChecked = preferenceApplier.isEnableSearchHistory
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
            else -> super.clickMenu(item);
        }
    }

    private fun initFavoriteModule() {
        disposables.add(
                Completable.create { e ->
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
                    e.onComplete()
                }
                        .subscribeOn(Schedulers.newThread())
                        .subscribe { favoriteModule.query("") }
        )
    }

    /**
     * Initialize history module asynchronously.
     */
    private fun initHistoryModule() {
        disposables.add(
                Completable.create { e ->
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
                    e.onComplete()
                }
                        .subscribeOn(Schedulers.newThread())
                        .subscribe {
                            if (preferenceApplier.isEnableSearchHistory) {
                                historyModule.query("")
                            }
                        }
        )
    }

    override fun onResume() {
        super.onResume()
        Inputs.toggle(this)

        applyColorToToolbar(binding?.toolbar as Toolbar)

        ImageLoader.setImageToImageView(binding?.background as ImageView, backgroundImagePath)
    }

    private fun initSearchInput() {
        binding?.searchInput?.setOnEditorActionListener({ v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(binding?.searchCategories?.selectedItem.toString(), v.text.toString())
            }
            true
        })
        binding?.searchInput?.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // NOP.
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                val key = s.toString()

                setActionButtonState(key.isEmpty())

                if (preferenceApplier.isEnableSearchHistory && historyModule != null) {
                    historyModule.query(s)
                }
                favoriteModule.query(s)

                if (preferenceApplier.isDisableSuggestion) {
                    suggestionModule.clear()
                    return
                }

                suggestionModule.request(key)
            }

            override fun afterTextChanged(s: Editable) {
                // NOP.
            }
        })
    }

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
        @ColorInt val bgColor = colorPair.bgColor()
        @ColorInt val fontColor = colorPair.fontColor()
        Colors.setEditTextColor(binding?.searchInput as EditText, fontColor)

        binding?.searchActionBackground?.setBackgroundColor(ColorUtils.setAlphaComponent(bgColor, 128))
        binding?.searchAction?.setColorFilter(fontColor)
        binding?.searchAction?.setOnClickListener({ view ->
            if (useVoice) {
                startActivityForResult(VoiceSearch.makeIntent(this), REQUEST_CODE_VOICE_SEARCH)
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
     */
    private fun search(category: String, query: String) {
        disposables.add(SearchAction(this, category, query).invoke())
    }

    /**
     * Hide software keyboard.
     */
    fun hideKeyboard() {
        if (binding?.searchInput != null) {
            Inputs.hideKeyboard(binding?.searchInput as EditText)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            REQUEST_CODE_VOICE_SEARCH -> {
                suggestionModule.show()
                val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (result?.size == 0) {
                    return
                }
                suggestionModule.clear()
                suggestionModule.addAll(result ?: emptyList())
            }
        }
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
        favoriteModule.dispose()
        historyModule.dispose()
        suggestionModule.dispose()
    }

    override fun titleId(): Int {
        return R.string.title_search
    }

    companion object {

        /** Layout ID.  */
        private val LAYOUT_ID = R.layout.activity_search

        private const val REQUEST_CODE_VOICE_SEARCH = 2

        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, SearchActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent;
        }

        const val REQUEST_CODE: Int = 21
    }
}
