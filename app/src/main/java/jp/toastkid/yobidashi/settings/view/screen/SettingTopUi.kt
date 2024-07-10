/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.search.view.setting.SearchSettingUi
import jp.toastkid.yobidashi.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingTopUi() {
    val contentViewModel = (LocalContext.current as? ViewModelStoreOwner)?.let {
        viewModel(ContentViewModel::class.java, it)
    }

    val pagerState = rememberPagerState { 8 }

    HorizontalPager(pageSize = PageSize.Fill, state = pagerState) { page ->
        SwitchContentWithTabIndex(page)
    }

    contentViewModel?.replaceAppBarContent {
        val pages = arrayOf(
            R.string.subhead_displaying,
            R.string.title_settings_color,
            R.string.search,
            R.string.subhead_browser,
            R.string.subhead_editor,
            R.string.title_calendar,
            R.string.title_color_filter,
            R.string.subhead_others
        )

        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 8.dp,
            containerColor = Color.Transparent,
            indicator = { tabPositions ->
                val primaryColor = MaterialTheme.colorScheme.onPrimary
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                        .height(2.dp)
                        .clip(RoundedCornerShape(8.dp)) // clip modifier not working
                        .padding(horizontal = 4.dp)
                        .drawBehind { drawRect(primaryColor) }
                )
            },
            modifier = Modifier.fillMaxHeight()
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
    }

    DisposableEffect(key1 = "refresh") {
        onDispose {
            contentViewModel?.refresh()
        }
    }

    contentViewModel?.clearOptionMenus()
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
