package jp.toastkid.yobidashi.search

import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.v4.graphics.ColorUtils
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
import android.widget.Spinner

import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.BaseFragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentSearchBinding
import jp.toastkid.yobidashi.databinding.ModuleSearchSuggestionBinding
import jp.toastkid.yobidashi.libs.Colors
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.search.history.HistoryModule
import jp.toastkid.yobidashi.search.history.SearchHistory
import jp.toastkid.yobidashi.search.history.SearchHistoryInsertion
import jp.toastkid.yobidashi.search.suggestion.SuggestionModule

/**
 * Search activity.

 * @author toastkidjp
 */
class SearchFragment : BaseFragment() {

    /** View binder.  */
    private var binding: FragmentSearchBinding? = null

    /** Preferences wrapper.  */
    private var preferenceApplier: PreferenceApplier? = null

    /** Disposables.  */
    private var disposables: CompositeDisposable? = null

    /** History module.  */
    private var historyModule: HistoryModule? = null

    /** Suggestion module.  */
    private var suggestionModule: SuggestionModule? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposables = CompositeDisposable()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        preferenceApplier = PreferenceApplier(context)
    }

    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate<FragmentSearchBinding>(inflater!!, LAYOUT_ID, container, false)
        binding?.searchClear?.setOnClickListener ({ v -> binding?.searchInput?.setText("") })
        SearchCategorySpinnerInitializer.initialize(binding?.searchCategories as Spinner)

        initHistoryModule()

        initSearchInput()

        applyColor()

        suggestionModule = SuggestionModule(
                binding?.suggestionModule as ModuleSearchSuggestionBinding,
                binding?.searchInput as EditText,
                Consumer<String> { suggestion -> search(binding?.searchCategories?.selectedItem.toString(), suggestion) },
                Runnable { this.hideKeyboard() }
        )

        setHasOptionsMenu(true)

        binding!!.scroll.setOnTouchListener({ v, event ->
            hideKeyboard()
            false
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding!!.searchBar.transitionName = "share"
        }

        return binding!!.root
    }

    /**
     * Initialize history module asynchronously.
     */
    private fun initHistoryModule() {
        disposables!!.add(
                Completable.create { e ->
                    historyModule = HistoryModule(
                            binding!!.historyModule,
                            Consumer<SearchHistory> { history -> search(history.category as String, history.query as String) },
                            Runnable { this.hideKeyboard() },
                            Consumer<String> { text ->
                                binding!!.searchInput.setText(text + " ")
                                binding!!.searchInput.setSelection(
                                        binding!!.searchInput.text.toString().length)
                            }
                    )
                    e.onComplete()
                }
                        .subscribeOn(Schedulers.newThread())
                        .subscribe {
                            if (preferenceApplier!!.isEnableSearchHistory) {
                                historyModule!!.query("")
                            }
                        }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.search_menu, menu)

        val item = menu!!.findItem(R.id.suggestion_check)
        item.isChecked = preferenceApplier!!.isEnableSuggestion
        item.setOnMenuItemClickListener { i ->
            preferenceApplier!!.switchEnableSuggestion()
            i.isChecked = preferenceApplier!!.isEnableSuggestion
            true
        }

        val history = menu.findItem(R.id.history_check)
        history.isChecked = preferenceApplier!!.isEnableSearchHistory
        history.setOnMenuItemClickListener { i ->
            preferenceApplier!!.switchEnableSearchHistory()
            i.isChecked = preferenceApplier!!.isEnableSearchHistory
            true
        }
    }

    override fun onResume() {
        super.onResume()
        Inputs.toggle(activity)
    }

    private fun initSearchInput() {
        binding!!.searchInput.setOnEditorActionListener({ v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(binding!!.searchCategories.selectedItem.toString(), v.text.toString())
            }
            true
        })
        binding!!.searchInput.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // NOP.
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                val key = s.toString()

                if (preferenceApplier!!.isEnableSearchHistory) {
                    historyModule!!.query(s)
                }

                if (preferenceApplier!!.isDisableSuggestion) {
                    suggestionModule!!.clear()
                    return
                }

                suggestionModule!!.request(key)
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
        val colorPair = preferenceApplier?.colorPair()
        @ColorInt val bgColor = colorPair?.bgColor() as Int
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
        binding?.searchIcon?.setColorFilter(bgColor)
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
        if (preferenceApplier!!.isEnableSearchHistory) {
            disposables!!.add(SearchHistoryInsertion.make(activity, category, query).insert())
        }
        SearchAction(activity, category, query).invoke()
    }

    /**
     * Hide software keyboard.
     */
    fun hideKeyboard() {
        if (binding == null) {
            return
        }
        Inputs.hideKeyboard(binding!!.searchInput)
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    override fun onDetach() {
        super.onDetach()
        disposables!!.dispose()
        historyModule!!.dispose()
        suggestionModule!!.dispose()
    }

    override fun titleId(): Int {
        return R.string.title_search
    }

    companion object {

        /** Layout ID.  */
        private val LAYOUT_ID = R.layout.fragment_search
    }
}
