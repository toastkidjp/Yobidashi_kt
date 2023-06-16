/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.search.view.setting

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.R
import jp.toastkid.search.SearchCategory
import jp.toastkid.search.view.SearchCategorySpinner
import jp.toastkid.ui.parts.InsetDivider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SearchSettingUi() {
    val activityContext = LocalContext.current
    val preferenceApplier = PreferenceApplier(activityContext)

    val contentViewModel = (activityContext as? ViewModelStoreOwner)?.let {
        viewModel(ContentViewModel::class.java, activityContext)
    }

    val spinnerOpen = remember { mutableStateOf(false) }
    val categoryName = remember {
        mutableStateOf(
            preferenceApplier.getDefaultSearchEngine()
                ?: SearchCategory.getDefaultCategoryName()
        )
    }

    val enableSearchQueryExtractCheck =
        remember { mutableStateOf(preferenceApplier.enableSearchQueryExtract) }
    val enableSearchWithClipCheck =
        remember { mutableStateOf(preferenceApplier.enableSearchWithClip) }
    val useSuggestionCheck = remember { mutableStateOf(preferenceApplier.isEnableSuggestion) }
    val useHistoryCheck = remember { mutableStateOf(preferenceApplier.isEnableSearchHistory) }
    val useFavoriteCheck = remember { mutableStateOf(preferenceApplier.isEnableFavoriteSearch) }
    val useViewHistoryCheck = remember { mutableStateOf(preferenceApplier.isEnableViewHistory) }
    val useUrlModuleCheck = remember { mutableStateOf(preferenceApplier.isEnableUrlModule()) }
    val useTrendCheck = remember { mutableStateOf(preferenceApplier.isEnableTrendModule()) }

    val selections = remember {
        SearchCategory.values()
            .map {
                SearchCategorySelection(
                    it,
                    mutableStateOf(preferenceApplier.readDisableSearchCategory()?.contains(it.name)?.not() ?: true)
                )
            }
    }

    Surface(shadowElevation = 4.dp, modifier = Modifier.padding(8.dp)) {
        LazyColumn {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        )
                        .clickable { spinnerOpen.value = true }
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_search_black),
                        contentDescription = stringResource(id = R.string.title_default_search_engine),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        stringResource(id = R.string.title_default_search_engine),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    )
                    SearchCategorySpinner(spinnerOpen, categoryName) {
                        preferenceApplier.setDefaultSearchEngine(it.name)
                    }
                }
            }

            item {
                InsetDivider()
            }

            item {
                CheckableRow(
                    textId = R.string.title_use_search_query_extractor,
                    clickable = {
                        val newState = !preferenceApplier.enableSearchQueryExtract
                        preferenceApplier.enableSearchQueryExtract = newState
                        enableSearchQueryExtractCheck.value =
                            preferenceApplier.enableSearchQueryExtract
                    },
                    booleanState = enableSearchQueryExtractCheck,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    iconId = R.drawable.ic_extract
                )
            }

            item {
                InsetDivider()
            }

            item {
                CheckableRow(
                    textId = R.string.title_enable_search_with_clip,
                    clickable = {
                        val newState = !preferenceApplier.enableSearchWithClip
                        preferenceApplier.enableSearchWithClip = newState

                        @StringRes val messageId: Int = if (newState) {
                            R.string.message_enable_swc
                        } else {
                            R.string.message_disable_swc
                        }
                        contentViewModel?.snackShort(messageId)

                        enableSearchWithClipCheck.value =
                            preferenceApplier.enableSearchWithClip
                    },
                    booleanState = enableSearchWithClipCheck,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    iconId = R.drawable.ic_clipboard_black
                )
            }

            item {
                InsetDivider()
            }

            item {
                CheckableRow(
                    textId = R.string.title_enable_suggestion,
                    clickable = {
                        preferenceApplier.switchEnableSuggestion()
                        useSuggestionCheck.value =
                            preferenceApplier.isEnableSuggestion
                    },
                    booleanState = useSuggestionCheck,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    iconId = R.drawable.ic_open_in_browser_black
                )
            }

            item {
                InsetDivider()
            }

            item {
                CheckableRow(
                    textId = R.string.title_use_search_history,
                    clickable = {
                        preferenceApplier.switchEnableSearchHistory()
                        useHistoryCheck.value = preferenceApplier.isEnableSearchHistory
                    },
                    booleanState = useHistoryCheck,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    iconId = R.drawable.ic_search_history_black
                )
            }

            item {
                InsetDivider()
            }

            item {
                CheckableRow(
                    textId = R.string.title_use_favorite_search,
                    clickable = {
                        preferenceApplier.switchEnableFavoriteSearch()
                        useFavoriteCheck.value = preferenceApplier.isEnableFavoriteSearch
                    },
                    booleanState = useFavoriteCheck,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    iconId = R.drawable.ic_favorite
                )
            }

            item {
                InsetDivider()
            }

            item {
                CheckableRow(
                    textId = R.string.use_view_history,
                    clickable = {
                        preferenceApplier.switchEnableViewHistory()
                        useViewHistoryCheck.value = preferenceApplier.isEnableViewHistory
                    },
                    booleanState = useViewHistoryCheck,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    iconId = R.drawable.ic_history_black
                )
            }

            item {
                InsetDivider()
            }

            item {
                CheckableRow(
                    textId = R.string.title_url_module,
                    clickable = {
                        preferenceApplier.switchEnableUrlModule()
                        useUrlModuleCheck.value = preferenceApplier.isEnableUrlModule()
                    },
                    booleanState = useUrlModuleCheck,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    iconId = R.drawable.ic_web_black
                )
            }

            item {
                InsetDivider()
            }

            item {
                CheckableRow(
                    textId = R.string.title_trend_module,
                    clickable = {
                        preferenceApplier.switchEnableTrendModule()
                        useTrendCheck.value = preferenceApplier.isEnableUrlModule()
                    },
                    booleanState = useTrendCheck,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    iconId = R.drawable.ic_trend_black
                )
            }

            item {
                InsetDivider()
            }

            item {
                val allChecked = remember { mutableStateOf(selections.all { it.checked.value }) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_search),
                        contentDescription = stringResource(id = R.string.subhead_search_category_setting),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        stringResource(id = R.string.subhead_search_category_setting),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    )
                    Checkbox(checked = allChecked.value, onCheckedChange = {
                        val newState = allChecked.value.not()
                        selections.forEach { it.checked.value = newState }
                        allChecked.value = newState
                    })
                }
            }

            items(selections, { it.searchCategory.id }) { selection ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable {
                            selection.checked.value = selection.checked.value.not()
                        }
                ) {
                    AsyncImage(
                        model = selection.searchCategory.iconId,
                        contentDescription = stringResource(id = selection.searchCategory.id),
                        modifier = Modifier.width(40.dp)
                    )
                    Text(
                        stringResource(id = selection.searchCategory.id),
                        fontSize = 20.sp,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(8.dp)
                    )
                    Checkbox(
                        checked = selection.checked.value,
                        onCheckedChange = {
                            selection.checked.value = selection.checked.value.not()
                        }
                    )
                }
            }
        }
    }

    val localLifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = localLifecycleOwner, effect = {
        onDispose {
            CoroutineScope(Dispatchers.IO).launch {
                preferenceApplier.clearDisableSearchCategory()
                selections.filter { it.checked.value.not() }
                    .forEach { preferenceApplier.addDisableSearchCategory(it.searchCategory.name) }
            }
        }
    })
}

/**
 * TODO temporary
 */
@Composable
internal fun CheckableRow(
    textId: Int,
    clickable: () -> Unit,
    booleanState: MutableState<Boolean>,
    iconTint: Color? = null,
    iconId: Int? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = clickable)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
    ) {
        if (iconId != null && iconTint != null) {
            Icon(
                painterResource(id = iconId),
                tint = iconTint,
                contentDescription = stringResource(id = textId),
                modifier = Modifier.padding(end = 4.dp)
            )
        }

        Text(
            stringResource(id = textId),
            modifier = Modifier
                .weight(1f)
        )
        Checkbox(
            checked = booleanState.value, onCheckedChange = { clickable() },
            modifier = Modifier.width(44.dp)
        )
    }
}