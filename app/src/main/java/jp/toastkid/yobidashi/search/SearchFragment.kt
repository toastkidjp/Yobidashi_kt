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
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.SearchCategory
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentSearchBinding
import jp.toastkid.yobidashi.libs.network.NetworkChecker
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchFragment
import jp.toastkid.yobidashi.search.history.SearchHistoryFragment
import jp.toastkid.yobidashi.search.usecase.ContentSwitcherUseCase
import jp.toastkid.yobidashi.search.voice.VoiceSearch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
     * Does use voice search?
     */
    private var useVoice: Boolean = true

    private lateinit var preferenceApplier: PreferenceApplier

    private var contentViewModel: ContentViewModel? = null

    private var appBarViewModel: AppBarViewModel? = null

    private var currentTitle: String? = null

    private var currentUrl: String? = null

    private var currentInput: MutableState<String>? = null

    private var currentCategory: MutableState<String>? = null

    private var contentSwitcherUseCase: ContentSwitcherUseCase? = null

    /**
     * Disposables.
     */
    private val disposables: Job by lazy { Job() }

    private val voiceSearchLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }
            val result = activityResult?.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (result == null || result.size == 0) {
                return@registerForActivityResult
            }
            binding?.suggestionCard?.replace(result)
            binding?.suggestionCard?.show()

            CoroutineScope(Dispatchers.Default).launch(disposables) {
                delay(200)
                withContext(Dispatchers.Main) {
                    val top = binding?.suggestionCard?.top ?: 0
                    binding?.scroll?.smoothScrollTo(0, top)
                }
            }
        }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)

        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)

        currentTitle = arguments?.getString(EXTRA_KEY_TITLE)
        currentUrl = arguments?.getString(EXTRA_KEY_URL)

        preferenceApplier = PreferenceApplier(context)

        activity?.also {
            val activityViewModelProvider = ViewModelProvider(it)
            appBarViewModel = activityViewModelProvider.get(AppBarViewModel::class.java)
            contentViewModel = activityViewModelProvider.get(ContentViewModel::class.java)
        }

        appBarViewModel?.replace(context) {
            val input = remember { mutableStateOf(currentUrl ?: "") }
            currentInput = input

            val spinnerOpen = remember { mutableStateOf(false) }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen.toolbar_height))
            ) {
                val categoryName = remember {
                    mutableStateOf(
                        (SearchCategory.findByUrlOrNull(currentUrl)?.name
                            ?: PreferenceApplier(context).getDefaultSearchEngine())
                            ?: SearchCategory.getDefaultCategoryName()
                    )
                }
                currentCategory = categoryName

                Box(
                    modifier = Modifier
                        .clickable {
                            spinnerOpen.value = true
                        }
                        .width(dimensionResource(id = R.dimen.search_category_spinner_width))
                        .fillMaxHeight()
                        .background(colorResource(id = R.color.spinner_background))
                ) {
                    val category = SearchCategory.findByCategory(currentCategory?.value)
                    Image(
                        painterResource(id = category.iconId),
                        contentDescription = stringResource(id = category.id),
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(4.dp)
                    )

                    DropdownMenu(
                        expanded = spinnerOpen.value,
                        onDismissRequest = { spinnerOpen.value = false }
                    ) {
                        val initialDisables = PreferenceApplier(context).readDisableSearchCategory()
                        val searchCategories = SearchCategory.values()
                            .filterNot { initialDisables?.contains(it.name) ?: false }
                        searchCategories.forEach { searchCategory ->
                            DropdownMenuItem(
                                onClick = {
                                    categoryName.value = searchCategory.name
                                    spinnerOpen.value = false
                                }
                            ) {
                                AsyncImage(
                                    model = searchCategory.iconId,
                                    contentDescription = stringResource(id = searchCategory.id),
                                    modifier = Modifier.width(40.dp)
                                )
                                Text(
                                    stringResource(id = searchCategory.id),
                                    color = colorResource(id = R.color.black),
                                    fontSize = 20.sp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }

                TextField(
                    value = input.value,
                    onValueChange = { text ->
                        input.value = text
                        contentSwitcherUseCase?.send(text)
                    },
                    label = {
                            Text(
                                stringResource(id = R.string.title_search),
                                color = Color(preferenceApplier.fontColor)
                            )
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color(preferenceApplier.fontColor),
                        textAlign = TextAlign.Start,
                    ),
                    trailingIcon = {
                        Icon(
                            Icons.Filled.Clear,
                            contentDescription = "clear text",
                            tint = Color(preferenceApplier.fontColor),
                            modifier = Modifier
                                //.offset(x = 8.dp)
                                .clickable {
                                    input.value = ""
                                }
                        )
                    },
                    maxLines = 1,
                    keyboardActions = KeyboardActions {
                        search(extractCurrentSearchCategory(), input.value)
                    },
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = true,
                        imeAction = ImeAction.Search
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                        .background(Color.Transparent)
                )

                Image(
                    painterResource(id = if (useVoice) R.drawable.ic_mic else R.drawable.ic_search_white),
                    contentDescription = stringResource(id = R.string.title_search_action),
                    colorFilter = ColorFilter.tint(Color(preferenceApplier.fontColor), BlendMode.SrcIn),
                    modifier = Modifier.width(32.dp).fillMaxHeight().align(Alignment.CenterVertically)
                        .combinedClickable(
                            true,
                            onClick = { invokeSearch() },
                            onLongClick = { invokeBackgroundSearch() }
                        )
                )
            }
        }

        contentSwitcherUseCase = ContentSwitcherUseCase(
            binding, preferenceApplier, this::setActionButtonState, currentTitle, currentUrl
        )

        contentSwitcherUseCase?.withDebounce()

        binding?.urlCard?.setInsertAction(this::setTextAndMoveCursorToEnd)

        setListenerForKeyboardHiding()

        contentViewModel?.snackShort(R.string.message_search_on_background)

        setHasOptionsMenu(true)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(this).get(SearchFragmentViewModel::class.java)
        viewModel.search
                .observe(viewLifecycleOwner, Observer { event ->
                    val searchEvent = event?.getContentIfNotHandled() ?: return@Observer
                    search(
                        searchEvent.category ?: extractCurrentSearchCategory(),
                        searchEvent.query,
                        searchEvent.background
                    )
                })
        viewModel.putQuery
                .observe(viewLifecycleOwner, Observer { event ->
                    val query = event?.getContentIfNotHandled() ?: return@Observer
                    setTextAndMoveCursorToEnd(query)
                })

        binding?.favoriteSearchCard?.setViewModel(viewModel)
        binding?.suggestionCard?.setViewModel(viewModel)
        binding?.searchHistoryCard?.setViewModel(viewModel)
        binding?.urlSuggestionCard?.setViewModel(viewModel)

        binding?.hourlyTrendCard?.setViewModel(viewModel)

        val query = arguments?.getString(EXTRA_KEY_QUERY) ?: ""
        setInitialQuery(query)
        arguments?.remove(EXTRA_KEY_QUERY)
    }

    private fun setInitialQuery(query: String?) {
        currentInput?.value = query ?: ""
        contentSwitcherUseCase?.invoke(query ?: "")
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListenerForKeyboardHiding() {
        binding?.scroll?.setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.search_menu, menu)

        menu.findItem(R.id.suggestion_check)?.isChecked = preferenceApplier.isEnableSuggestion
        menu.findItem(R.id.history_check)?.isChecked = preferenceApplier.isEnableSearchHistory
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.double_quote -> {
            val queryOrEmpty = currentInput?.value
            if (queryOrEmpty?.isNotBlank() == true) {
                setTextAndMoveCursorToEnd("\"$queryOrEmpty\"")
            }
            true
        }
        R.id.set_default_search_category -> {
            currentCategory?.value = preferenceApplier.getDefaultSearchEngine()
                ?: SearchCategory.getDefaultCategoryName()
            true
        }
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
                ViewModelProvider(it)
                        .get(ContentViewModel::class.java)
                        .nextFragment(FavoriteSearchFragment::class.java)
            }
            true
        }
        R.id.open_search_history -> {
            activity?.also {
                ViewModelProvider(it)
                        .get(ContentViewModel::class.java)
                        .nextFragment(SearchHistoryFragment::class.java)
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun setTextAndMoveCursorToEnd(text: String) {
        currentInput?.value = text
    }

    override fun onResume() {
        super.onResume()

        binding?.suggestionCard?.isEnabled = preferenceApplier.isEnableSuggestion
        binding?.searchHistoryCard?.isEnabled = preferenceApplier.isEnableSearchHistory
        binding?.favoriteSearchCard?.isEnabled = preferenceApplier.isEnableFavoriteSearch
        binding?.urlCard?.isEnabled = preferenceApplier.isEnableUrlModule()
        binding?.urlSuggestionCard?.isEnabled = preferenceApplier.isEnableViewHistory
        binding?.hourlyTrendCard?.isEnabled = preferenceApplier.isEnableTrendModule()

        binding?.hourlyTrendCard?.request()

        binding?.urlCard?.onResume()

        showKeyboard()
    }

    private fun showKeyboard() {
        /*TODO activity?.let {
            val input = headerBinding?.searchInput ?: return@let
            if (!input.requestFocus()) {
                return@let
            }

            val inputMethodManager =
                    it.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                            ?: return@let

            input.postDelayed(
                    { inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_FORCED) },
                    100L
            )
        }*/
    }

    /**
     * Set action button state.
     *
     * @param useVoiceSearch
     */
    private fun setActionButtonState(useVoiceSearch: Boolean) {
        this.useVoice = useVoiceSearch
    }

    fun invokeSearch() {
        if (useVoice) {
            invokeVoiceSearch()
            return
        }
        search(
                extractCurrentSearchCategory(),
                currentInput?.value.toString()
        )
    }

    private fun invokeVoiceSearch() {
        try {
            context?.let {
                voiceSearchLauncher.launch(VoiceSearch().makeIntent(it))
            }
        } catch (e: ActivityNotFoundException) {
            Timber.e(e)
            VoiceSearch().suggestInstallGoogleApp(binding?.root as View, preferenceApplier.colorPair())
        }
    }

    fun invokeBackgroundSearch(): Boolean {
        if (useVoice) {
            return true
        }

        search(
                extractCurrentSearchCategory(),
                currentInput?.value.toString(),
                true
        )
        return true
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
        val context = context ?: return
        if (NetworkChecker.isNotAvailable(context)) {
            contentViewModel?.snackShort("Network is not available...")
            return
        }

        SearchAction(context, category, query, currentUrl, onBackground).invoke()

        arguments?.clear()
    }

    fun clearInput() {
        currentInput?.value = ""
    }

    /**
     * Hide software keyboard.
     * TODO should deactivate
     */
    private fun hideKeyboard() {
        //TODO headerBinding?.searchInput?.let { Inputs.hideKeyboard(it) }
    }

    private fun extractCurrentSearchCategory() =
            currentCategory?.value ?: ""

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    override fun onDetach() {
        hideKeyboard()
        disposables.cancel()
        contentSwitcherUseCase?.dispose()
        binding?.favoriteSearchCard?.dispose()
        binding?.searchHistoryCard?.dispose()
        binding?.suggestionCard?.dispose()
        binding?.hourlyTrendCard?.dispose()
        binding?.urlSuggestionCard?.dispose()
        binding?.urlCard?.dispose()
        voiceSearchLauncher.unregister()
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
         */
        fun makeWith(title: String? = null, url: String? = null) =
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
         * @param query Query
         * @param title Title
         * @param url URL
         */
        fun makeWithQuery(query: String, title: String?, url: String? = null) =
                makeWith(title, url).also { fragment ->
                    fragment.arguments?.putString(EXTRA_KEY_QUERY, query)
                }

    }
}