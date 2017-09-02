package jp.toastkid.yobidashi.search

import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.v4.graphics.ColorUtils
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Spinner
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.BaseFragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentSearchBinding
import jp.toastkid.yobidashi.databinding.ModuleSearchFavoriteBinding
import jp.toastkid.yobidashi.databinding.ModuleSearchHistoryBinding
import jp.toastkid.yobidashi.databinding.ModuleSearchSuggestionBinding
import jp.toastkid.yobidashi.libs.Colors
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchActivity
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchModule
import jp.toastkid.yobidashi.search.history.HistoryModule
import jp.toastkid.yobidashi.search.history.SearchHistoryActivity
import jp.toastkid.yobidashi.search.suggestion.SuggestionModule

/**
 * Search activity.

 * @author toastkidjp
 */
class SearchFragment : BaseFragment() {

    /** View binder.  */
    private var binding: FragmentSearchBinding? = null

    /** Disposables.  */
    private val disposables: CompositeDisposable = CompositeDisposable()


    private lateinit var favoriteModule: FavoriteSearchModule

    /** History module.  */
    private lateinit var historyModule: HistoryModule

    /** Suggestion module.  */
    private lateinit var suggestionModule: SuggestionModule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate<FragmentSearchBinding>(inflater, LAYOUT_ID, container, false)
        binding?.searchClear?.setOnClickListener ({ v -> binding?.searchInput?.setText("") })
        SearchCategorySpinnerInitializer.initialize(binding?.searchCategories as Spinner)

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

        setHasOptionsMenu(true)

        binding?.scroll?.setOnTouchListener({ v, event ->
            hideKeyboard()
            false
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding?.searchBar?.transitionName = "share"
        }

        return binding?.root
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
                        .subscribe {
                            favoriteModule.query("")
                        }
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
                            if (preferenceApplier().isEnableSearchHistory) {
                                historyModule.query("")
                            }
                        }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.search_menu, menu)

        val preferenceApplier: PreferenceApplier = preferenceApplier()
        val item = menu!!.findItem(R.id.suggestion_check)
        item.isChecked = preferenceApplier.isEnableSuggestion
        item.setOnMenuItemClickListener { i ->
            preferenceApplier.switchEnableSuggestion()
            i.isChecked = preferenceApplier.isEnableSuggestion
            true
        }

        val history = menu.findItem(R.id.history_check)
        history.isChecked = preferenceApplier.isEnableSearchHistory
        history.setOnMenuItemClickListener { i ->
            preferenceApplier.switchEnableSearchHistory()
            i.isChecked = preferenceApplier.isEnableSearchHistory
            true
        }

        menu.findItem(R.id.open_favorite_search).setOnMenuItemClickListener {
            activity.startActivity(FavoriteSearchActivity.makeIntent(activity))
            true
        }

        menu.findItem(R.id.open_search_history).setOnMenuItemClickListener {
            activity.startActivity(SearchHistoryActivity.makeIntent(activity))
            true
        }
    }

    override fun onResume() {
        super.onResume()
        Inputs.toggle(activity)
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

                val preferenceApplier: PreferenceApplier = preferenceApplier()
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

    /**
     * Apply color to views.
     */
    private fun applyColor() {
        val colorPair : ColorPair = colorPair()
        @ColorInt val bgColor = colorPair.bgColor()
        @ColorInt val fontColor = colorPair.fontColor()
        Colors.setEditTextColor(binding?.searchInput as EditText, bgColor)

        binding?.searchActionBackground?.setBackgroundColor(ColorUtils.setAlphaComponent(bgColor, 128))
        binding?.searchAction?.setTextColor(fontColor)
        binding?.searchAction?.setOnClickListener({ view ->
            search(
                    binding?.searchCategories?.selectedItem.toString(),
                    binding?.searchInput?.text.toString()
            )
        })
        binding?.searchClear?.setColorFilter(bgColor)
        binding?.searchInputBorder?.setBackgroundColor(bgColor)
    }

    /**
     * Open search result.

     * @param category search category
     * *
     * @param query    search query
     */
    private fun search(category: String, query: String) {
        disposables.add(SearchAction(activity, category, query).invoke())
    }

    /**
     * Hide software keyboard.
     */
    fun hideKeyboard() {
        if (binding?.searchInput != null) {
            Inputs.hideKeyboard(binding?.searchInput as EditText)
        }
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    override fun onDetach() {
        super.onDetach()
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
        private val LAYOUT_ID = R.layout.fragment_search
    }
}
