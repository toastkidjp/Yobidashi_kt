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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.R
import jp.toastkid.search.SearchCategory
import jp.toastkid.search.view.SearchCategorySpinner
import jp.toastkid.ui.image.EfficientImage
import jp.toastkid.ui.parts.CheckableRow
import jp.toastkid.ui.parts.InsetDivider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SearchSettingUi() {
    val activityContext = LocalContext.current
    val preferenceApplier = remember { PreferenceApplier(activityContext) }

    val contentViewModel = (activityContext as? ViewModelStoreOwner)?.let {
        viewModel(ContentViewModel::class.java, activityContext)
    }

    val viewModel = remember { SearchSettingViewModel() }

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
                        .clickable { viewModel.openSpinner() }
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
                    SearchCategorySpinner(
                        viewModel::spinnerOpen,
                        { viewModel.openSpinner() },
                        { viewModel.closeSpinner() },
                        viewModel.categoryName()
                    ) {
                        preferenceApplier.setDefaultSearchEngine(it.name)
                        viewModel.setCategoryName(it.name)
                    }
                }
            }

            item {
                InsetDivider()
            }

            item {
                CheckableRow(
                    textId = R.string.title_use_search_query_extractor,
                    onSwitch = {
                        val newState = !preferenceApplier.enableSearchQueryExtract
                        preferenceApplier.enableSearchQueryExtract = newState
                        viewModel.setEnableSearchQueryExtractCheck(preferenceApplier.enableSearchQueryExtract)
                    },
                    checked = viewModel::enableSearchQueryExtractCheck,
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
                    onSwitch = {
                        val newState = !preferenceApplier.enableSearchWithClip
                        preferenceApplier.enableSearchWithClip = newState

                        @StringRes val messageId: Int = if (newState) {
                            R.string.message_enable_swc
                        } else {
                            R.string.message_disable_swc
                        }
                        contentViewModel?.snackShort(messageId)

                        viewModel.setEnableSearchWithClipCheck(preferenceApplier.enableSearchWithClip)

                    },
                    checked = viewModel::enableSearchWithClipCheck,
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
                    onSwitch = {
                        preferenceApplier.switchEnableSuggestion()
                        viewModel.setUseSuggestionCheck(preferenceApplier.isEnableSuggestion)
                    },
                    checked = viewModel::useSuggestionCheck,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    iconId = R.drawable.ic_search_black
                )
            }

            item {
                InsetDivider()
            }

            item {
                CheckableRow(
                    textId = R.string.title_use_search_history,
                    onSwitch = {
                        preferenceApplier.switchEnableSearchHistory()
                        viewModel.setUseHistoryCheck(preferenceApplier.isEnableSearchHistory)
                    },
                    checked = viewModel::useHistoryCheck,
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
                    onSwitch = {
                        preferenceApplier.switchEnableFavoriteSearch()
                        viewModel.setUseFavoriteCheck(preferenceApplier.isEnableFavoriteSearch)
                    },
                    checked = viewModel::useFavoriteCheck,
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
                    onSwitch = {
                        preferenceApplier.switchEnableViewHistory()
                        viewModel.setUseViewHistoryCheck(preferenceApplier.isEnableViewHistory)
                    },
                    checked = viewModel::useViewHistoryCheck,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    iconId = jp.toastkid.lib.R.drawable.ic_history_black
                )
            }

            item {
                InsetDivider()
            }

            item {
                CheckableRow(
                    textId = R.string.title_url_module,
                    checked = viewModel::useUrlModuleCheck,
                    onSwitch = {
                        preferenceApplier.switchEnableUrlModule()
                        viewModel.setUseUrlModuleCheck(preferenceApplier.isEnableUrlModule())
                    },
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
                    onSwitch = {
                        preferenceApplier.switchEnableTrendModule()
                        viewModel.setUseTrendCheck(preferenceApplier.isEnableUrlModule())
                    },
                    checked = viewModel::useTrendCheck,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    iconId = R.drawable.ic_trend_black
                )
            }

            item {
                InsetDivider()
            }

            item {
                val allChecked = remember { mutableStateOf(viewModel.selections().size == SearchCategory.entries.size) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(
                        painterResource(id = jp.toastkid.lib.R.drawable.ic_search),
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
                        allChecked.value = newState

                        viewModel.onSwitchSelectAll(newState)
                    })
                }
            }

            items(SearchCategory.entries, SearchCategory::id) { selection ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable {
                            viewModel.onSelectionChange(selection)
                        }
                ) {
                    EfficientImage(
                        model = selection.iconId,
                        contentDescription = stringResource(id = selection.id),
                        modifier = Modifier.width(40.dp)
                    )
                    Text(
                        stringResource(id = selection.id),
                        fontSize = 20.sp,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(8.dp)
                    )
                    Checkbox(
                        checked = viewModel.selections().contains(selection),
                        onCheckedChange = {
                            viewModel.onSelectionChange(selection)
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
                SearchCategory.entries
                    .filterNot(viewModel.selections()::contains)
                    .forEach { preferenceApplier.addDisableSearchCategory(it.name) }
            }
        }
    })

    LaunchedEffect(Unit) {
        viewModel.launch(preferenceApplier)
    }
}
