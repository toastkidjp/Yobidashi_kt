/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings.view.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import coil.compose.AsyncImage
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.color.IconColorFinder
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.SearchCategory
import jp.toastkid.ui.parts.InsetDivider
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.search.view.SearchCategorySpinner
import jp.toastkid.yobidashi.settings.fragment.search.category.SearchCategorySelection
import jp.toastkid.yobidashi.settings.view.CheckableRow

@Composable
internal fun SearchSettingUi() {
    val activityContext = LocalContext.current
    val preferenceApplier = PreferenceApplier(activityContext)

    val contentViewModel = (activityContext as? ViewModelStoreOwner)?.let {
        ViewModelProvider(activityContext).get(ContentViewModel::class.java)
    }

    val iconTint = Color(IconColorFinder.from(activityContext).invoke())
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
                    preferenceApplier.readDisableSearchCategory()?.contains(it.name)?.not() ?: true
                )
            }
    }

    Surface(elevation = 4.dp, modifier = Modifier.padding(8.dp)) {
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
                        tint = iconTint
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
                    iconTint = iconTint,
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
                    iconTint = iconTint,
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
                    iconTint = iconTint,
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
                    iconTint = iconTint,
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
                    iconTint = iconTint,
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
                    iconTint = iconTint,
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
                    iconTint = iconTint,
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
                    iconTint = iconTint,
                    iconId = R.drawable.ic_trend_black
                )
            }

            item {
                InsetDivider()
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(id = R.drawable.ic_search),
                        contentDescription = stringResource(id = R.string.subhead_search_category_setting),
                        tint = iconTint
                    )
                    Text(
                        stringResource(id = R.string.subhead_search_category_setting),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    )
                    Checkbox(checked = false, onCheckedChange = {

                    })
                }
            }

            items(selections, { it.searchCategory.id }) { selection ->
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        checked = selection.checked,
                        onCheckedChange = {
                            selection.checked = selection.checked.not()
                        }
                    )
                }
            }
        }
    }
}
