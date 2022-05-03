/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.view

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.SearchCategory
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.libs.network.NetworkChecker
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.url_suggestion.QueryUseCase
import jp.toastkid.yobidashi.search.usecase.QueryingUseCase
import jp.toastkid.yobidashi.search.viewmodel.SearchUiViewModel
import jp.toastkid.yobidashi.search.voice.VoiceSearch
import timber.log.Timber

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchInputUi(
    inputQuery: String? = null,
    currentTitle: String? = null,
    currentUrl: String? = null
) {
    val context = LocalContext.current as? ComponentActivity ?: return

    val preferenceApplier = PreferenceApplier(context)

    val activityViewModelProvider = ViewModelProvider(context)
    val appBarViewModel = activityViewModelProvider.get(AppBarViewModel::class.java)
    val contentViewModel = activityViewModelProvider.get(ContentViewModel::class.java)

    val categoryName = remember {
        mutableStateOf(
            (SearchCategory.findByUrlOrNull(currentUrl)?.name
                ?: PreferenceApplier(context).getDefaultSearchEngine())
                ?: SearchCategory.getDefaultCategoryName()
        )
    }

    val inputState = remember {
        val text = inputQuery ?: ""
        mutableStateOf(TextFieldValue(text, TextRange(0, text.length), TextRange(text.length)))
    }

    val viewModel = ViewModelProvider(context).get(SearchUiViewModel::class.java)

    val voiceSearchLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode != Activity.RESULT_OK) {
                return@rememberLauncherForActivityResult
            }
            val result =
                activityResult?.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (result == null || result.size == 0) {
                return@rememberLauncherForActivityResult
            }

            viewModel.suggestions.clear()
            viewModel.suggestions.addAll(result)
        }

    val database = DatabaseFinder().invoke(context)
    val queryingUseCase = QueryingUseCase(
        viewModel,
        preferenceApplier,
        QueryUseCase(
            {
                viewModel.urlItems.clear()
                viewModel.urlItems.addAll(it)
            },
            database.bookmarkRepository(),
            database.viewHistoryRepository(),
            { }
        ),
        database.favoriteSearchRepository(),
        database.searchHistoryRepository()
    )

    val keyboardController = LocalSoftwareKeyboardController.current

    appBarViewModel.replace {
        val spinnerOpen = remember { mutableStateOf(false) }

        val useVoice = remember { mutableStateOf(false) }

        val focusRequester = remember { FocusRequester() }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.toolbar_height))
        ) {
            SearchCategorySpinner(spinnerOpen, categoryName)

            TextField(
                value = inputState.value,
                onValueChange = { text ->
                    inputState.value = text
                    useVoice.value = text.text.isBlank()
                    queryingUseCase.send(text.text)
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
                                inputState.value = TextFieldValue()
                            }
                    )
                },
                maxLines = 1,
                keyboardActions = KeyboardActions {
                    keyboardController?.hide()
                    search(context, contentViewModel, currentUrl, categoryName.value, inputState.value.text)
                },
                keyboardOptions = KeyboardOptions(
                    autoCorrect = true,
                    imeAction = ImeAction.Search
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
                    .background(Color.Transparent)
                    .focusRequester(focusRequester)
            )

            Image(
                painterResource(id = if (useVoice.value) R.drawable.ic_mic else R.drawable.ic_search_white),
                contentDescription = stringResource(id = R.string.title_search_action),
                colorFilter = ColorFilter.tint(Color(preferenceApplier.fontColor), BlendMode.SrcIn),
                modifier = Modifier
                    .width(32.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically)
                    .combinedClickable(
                        true,
                        onClick = {
                            keyboardController?.hide()

                            if (useVoice.value) {
                                invokeVoiceSearch(voiceSearchLauncher)
                                return@combinedClickable
                            }

                            search(
                                context,
                                contentViewModel,
                                currentUrl,
                                categoryName.value,
                                inputState.value.text
                            )
                        },
                        onLongClick = {
                            search(
                                context,
                                contentViewModel,
                                currentUrl,
                                categoryName.value,
                                inputState.value.text,
                                true
                            )
                        }
                    )
            )
        }

        LaunchedEffect(key1 = "first_launch", block = {
            focusRequester.requestFocus()
            keyboardController?.show()
        })
    }

    queryingUseCase.withDebounce()

    SearchContentsUi(viewModel, currentTitle, currentUrl)

    queryingUseCase.send("")

    viewModel.search
        .observe(context, Observer { event ->
            keyboardController?.hide()

            val searchEvent = event?.getContentIfNotHandled() ?: return@Observer
            search(
                context,
                contentViewModel,
                currentUrl,
                searchEvent.category ?: categoryName.value,
                searchEvent.query,
                searchEvent.background
            )
        })
    viewModel.putQuery
        .observe(context, Observer { event ->
            val query = event?.getContentIfNotHandled() ?: return@Observer
            inputState.value = TextFieldValue(query, TextRange(query.length), TextRange.Zero)
        })

    val isEnableSuggestion = remember { mutableStateOf(preferenceApplier.isEnableSuggestion) }
    val isEnableSearchHistory = remember { mutableStateOf(preferenceApplier.isEnableSearchHistory) }

    contentViewModel.optionMenus(
        OptionMenu(
            titleId = R.string.title_context_editor_double_quote,
            action = {
                val queryOrEmpty = inputState.value.text
                if (queryOrEmpty.isNotBlank()) {
                    viewModel.putQuery("\"$queryOrEmpty\"")
                }
            }
        ),
        OptionMenu(
            titleId = R.string.title_context_editor_set_default_search_category,
            action = {
                categoryName.value = preferenceApplier.getDefaultSearchEngine()
                    ?: SearchCategory.getDefaultCategoryName()
            }
        ),
        OptionMenu(
            titleId = R.string.title_enable_suggestion,
            action = {
                preferenceApplier.switchEnableSuggestion()
                isEnableSuggestion.value = preferenceApplier.isEnableSuggestion
                if (preferenceApplier.isEnableSuggestion.not()) {
                    viewModel.suggestions.clear()
                }
            },
            checkState = isEnableSuggestion
        ),
        OptionMenu(
            titleId = R.string.title_use_search_history,
            action = {
                preferenceApplier.switchEnableSearchHistory()
                isEnableSearchHistory.value = preferenceApplier.isEnableSearchHistory
                if (preferenceApplier.isEnableSearchHistory.not()) {
                    viewModel.searchHistories.clear()
                }
            },
            checkState = isEnableSearchHistory
        ),
        OptionMenu(
            titleId = R.string.title_favorite_search,
            action = {
                contentViewModel.nextRoute("search/favorite/list")

            }
        ),
        OptionMenu(
            titleId = R.string.title_search_history,
            action = {
                contentViewModel.nextRoute("search/history/list")
            }
        )
    )
}

private fun invokeVoiceSearch(
    voiceSearchLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
) {
    try {
        voiceSearchLauncher.launch(VoiceSearch().makeIntent())
    } catch (e: ActivityNotFoundException) {
        Timber.e(e)
    }
}

private inline fun search(
    context: Context,
    contentViewModel: ContentViewModel?,
    currentUrl: String?,
    category: String,
    query: String,
    onBackground: Boolean = false
) {
    if (NetworkChecker.isNotAvailable(context)) {
        contentViewModel?.snackShort("Network is not available...")
        return
    }

    SearchAction(context, category, query, currentUrl, onBackground).invoke()
}

/*
TODO menu
        inflater.inflate(R.menu.search_menu, menu)

        menu.findItem(R.id.suggestion_check)?.isChecked = preferenceApplier.isEnableSuggestion
        menu.findItem(R.id.history_check)?.isChecked = preferenceApplier.isEnableSearchHistory
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
 */