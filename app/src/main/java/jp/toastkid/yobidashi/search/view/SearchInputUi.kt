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
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
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
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.SearchCategory
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.libs.network.NetworkChecker
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchListUi
import jp.toastkid.yobidashi.search.history.SearchHistoryListUi
import jp.toastkid.yobidashi.search.trend.TrendApi
import jp.toastkid.yobidashi.search.url_suggestion.QueryUseCase
import jp.toastkid.yobidashi.search.usecase.QueryingUseCase
import jp.toastkid.yobidashi.search.viewmodel.SearchUiViewModel
import jp.toastkid.yobidashi.search.voice.VoiceSearchIntentFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException

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
    val contentViewModel = activityViewModelProvider.get(ContentViewModel::class.java)

    val categoryName = remember {
        mutableStateOf(
            (SearchCategory.findByUrlOrNull(currentUrl)?.name
                ?: PreferenceApplier(context).getDefaultSearchEngine())
                ?: SearchCategory.getDefaultCategoryName()
        )
    }

    val viewModel = viewModel(SearchUiViewModel::class.java)

    val queryingUseCase = remember {
        val database = DatabaseFinder().invoke(context)

        QueryingUseCase(
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
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    val localLifecycleOwner = LocalLifecycleOwner.current

    if (viewModel.openFavoriteSearch.value.not()) {
        LaunchedEffect(key1 = localLifecycleOwner, block = {
            contentViewModel.replaceAppBarContent {
                val focusRequester = remember { FocusRequester() }

                val spinnerOpen = remember { mutableStateOf(false) }

                val useVoice = remember { mutableStateOf(false) }

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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(id = R.dimen.toolbar_height))
                ) {
                    SearchCategorySpinner(spinnerOpen, categoryName)

                    TextField(
                        value = viewModel.input.value,
                        onValueChange = { text ->
                            viewModel.setInput(text)
                            useVoice.value = text.text.isBlank()
                            queryingUseCase.send(text.text)
                        },
                        label = {
                            Text(
                                stringResource(id = R.string.title_search),
                                color = MaterialTheme.colors.onPrimary
                            )
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = MaterialTheme.colors.onPrimary,
                            textAlign = TextAlign.Start,
                        ),
                        trailingIcon = {
                            Icon(
                                Icons.Filled.Clear,
                                contentDescription = "clear text",
                                tint = MaterialTheme.colors.onPrimary,
                                modifier = Modifier
                                    //.offset(x = 8.dp)
                                    .clickable {
                                        viewModel.setInput(TextFieldValue())
                                    }
                            )
                        },
                        maxLines = 1,
                        keyboardActions = KeyboardActions {
                            keyboardController?.hide()
                            search(context, contentViewModel, currentUrl, categoryName.value, viewModel.input.value.text)
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

                    Icon(
                        painterResource(id = if (useVoice.value) R.drawable.ic_mic else R.drawable.ic_search_white),
                        contentDescription = stringResource(id = R.string.title_search_action),
                        tint = MaterialTheme.colors.onPrimary,
                        modifier = Modifier
                            .width(32.dp)
                            .fillMaxHeight()
                            .align(Alignment.CenterVertically)
                            .combinedClickable(
                                true,
                                onClick = {
                                    keyboardController?.hide()

                                    if (useVoice.value) {
                                        try {
                                            voiceSearchLauncher.launch(VoiceSearchIntentFactory().invoke())
                                        } catch (e: ActivityNotFoundException) {
                                            Timber.e(e)
                                        }
                                        return@combinedClickable
                                    }

                                    search(
                                        context,
                                        contentViewModel,
                                        currentUrl,
                                        categoryName.value,
                                        viewModel.input.value.text
                                    )
                                },
                                onLongClick = {
                                    search(
                                        context,
                                        contentViewModel,
                                        currentUrl,
                                        categoryName.value,
                                        viewModel.input.value.text,
                                        true
                                    )
                                }
                            )
                    )
                }
                LaunchedEffect(key1 = queryingUseCase, block = {
                    val text = inputQuery ?: ""
                    viewModel.setInput(TextFieldValue(text, TextRange(0, text.length), TextRange(text.length)))
                    focusRequester.requestFocus()

                    CoroutineScope(Dispatchers.IO).launch {
                        val trendItems = try {
                            TrendApi()()
                        } catch (e: IOException) {
                            Timber.e(e)
                            null
                        }
                        viewModel.trends.clear()
                        val taken = trendItems?.take(10)
                        if (taken.isNullOrEmpty()) {
                            return@launch
                        }
                        viewModel.trends.addAll(taken)
                    }

                    queryingUseCase.withDebounce()
                })
            }
        })
    }

    if (viewModel.enableBackHandler().not()) {
        SearchContentsUi(viewModel, currentTitle, currentUrl)
    }

    if (viewModel.openSearchHistory.value) {
        SearchHistoryListUi()
    }

    if (viewModel.openFavoriteSearch.value) {
        FavoriteSearchListUi()
    }
    
    BackHandler(viewModel.enableBackHandler()) {
        viewModel.closeOption()
    }

    viewModel.search
        .observe(localLifecycleOwner, Observer { event ->
            val searchEvent = event?.getContentIfNotHandled() ?: return@Observer
            if (searchEvent.query.isBlank()) {
                contentViewModel.snackShort(R.string.message_should_input_keyword)
                return@Observer
            }

            keyboardController?.hide()

            search(
                context,
                contentViewModel,
                currentUrl,
                searchEvent.category ?: categoryName.value,
                searchEvent.query,
                searchEvent.background
            )
        })

    /*LaunchedEffect(key1 = queryingUseCase.hashCode(), block = {
        val text = inputQuery ?: ""
        viewModel.setInput(TextFieldValue(text, TextRange(0, text.length), TextRange(text.length)))

        queryingUseCase.withDebounce()

        CoroutineScope(Dispatchers.IO).launch {
            val trendItems = try {
                TrendApi()()
            } catch (e: IOException) {
                Timber.e(e)
                null
            }
            viewModel.trends.clear()
            val taken = trendItems?.take(10)
            if (taken.isNullOrEmpty()) {
                return@launch
            }
            viewModel.trends.addAll(taken)
        }
    })*/

    DisposableEffect(key1 = localLifecycleOwner, effect = {
        onDispose {
            queryingUseCase.dispose()
        }
    })

    val isEnableSuggestion = remember { mutableStateOf(preferenceApplier.isEnableSuggestion) }
    val isEnableSearchHistory = remember { mutableStateOf(preferenceApplier.isEnableSearchHistory) }

    contentViewModel.optionMenus(
        OptionMenu(
            titleId = R.string.title_context_editor_double_quote,
            action = {
                val queryOrEmpty = viewModel.input.value.text
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
                viewModel.openFavoriteSearch()
            }
        ),
        OptionMenu(
            titleId = R.string.title_search_history,
            action = {
                viewModel.openSearchHistory()
            }
        )
    )
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