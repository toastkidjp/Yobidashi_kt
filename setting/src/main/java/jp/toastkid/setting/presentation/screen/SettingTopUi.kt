/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.setting.presentation.screen

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.search.view.setting.SearchSettingUi
import jp.toastkid.setting.R
import kotlinx.coroutines.launch

@Composable
fun SettingTopUi() {
    val activity = LocalActivity.current as? ComponentActivity ?: return
    val contentViewModel = viewModel(ContentViewModel::class.java, activity)

    val pagerState = rememberPagerState { 8 }

    HorizontalPager(pageSize = PageSize.Fill, state = pagerState) { page ->
        SwitchContentWithTabIndex(page)
    }

    LaunchedEffect(key1 = pagerState.currentPage) {
        contentViewModel.clearOptionMenus()
    }

    DisposableEffect(key1 = LocalLifecycleOwner.current) {
        contentViewModel.replaceAppBarContent {
            val pages = arrayOf(
                R.string.subhead_displaying,
                R.string.title_settings_color,
                jp.toastkid.lib.R.string.search,
                R.string.subhead_browser,
                R.string.subhead_editor,
                R.string.title_calendar,
                R.string.title_color_filter,
                R.string.subhead_others
            )

            Column(
                modifier = Modifier.fillMaxHeight()
            ) {
                PrimaryScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    edgePadding = 8.dp,
                    containerColor = Color.Transparent,
                    indicator = {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier
                                .tabIndicatorOffset(
                                    selectedTabIndex = pagerState.currentPage,
                                )
                                .background(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                                ),
                            width = Dp.Unspecified,
                            height = 2.dp,
                            color = Color.Transparent
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    pages.forEachIndexed { index, page ->
                        val coroutineScope = rememberCoroutineScope()
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                        ) {
                            Text(
                                text = stringResource(id = page),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        onDispose(contentViewModel::refresh)
    }
}


@Composable
private fun SwitchContentWithTabIndex(selectedIndex: Int) {
    when (selectedIndex) {
        0 -> DisplaySettingUi()
        1 -> ColorSettingUi()
        2 -> SearchSettingUi()
        3 -> WebSettingUi()
        4 -> EditorSettingUi()
        5 -> CalendarSettingUi()
        6 -> ColorFilterSettingUi()
        7 -> OtherSettingUi()
        else -> DisplaySettingUi()
    }
}
