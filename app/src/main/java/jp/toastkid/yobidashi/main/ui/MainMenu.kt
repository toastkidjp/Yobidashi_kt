/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.yobidashi.menu.Menu

@Composable
fun MainMenu(
    openFindInPageState: MutableState<Boolean>,
    navigate: (String) -> Unit,
    showAudioPlayer: () -> Unit,
    hideMenu: () -> Unit
) {
    val activity = LocalContext.current as? ComponentActivity ?: return
    val contentViewModel = viewModel(ContentViewModel::class.java, activity)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        val menuCount = Menu.values().size
        val tooBigCount = menuCount * 10
        LazyRow(
            state = rememberLazyListState(tooBigCount / 2),
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
        ) {
            items(tooBigCount) { longIndex ->
                val menu = Menu.values().get(longIndex % menuCount)
                Surface(
                    color = MaterialTheme.colors.primary,
                    elevation = 4.dp,
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable {
                            onClickMainMenuItem(
                                menu,
                                contentViewModel,
                                showAudioPlayer,
                                navigate,
                                openFindInPageState
                            )

                            hideMenu()
                        }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .size(76.dp)
                            .padding(4.dp)
                    ) {
                        Icon(
                            painterResource(id = menu.iconId),
                            contentDescription = stringResource(id = menu.titleId),
                            tint = MaterialTheme.colors.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            stringResource(id = menu.titleId),
                            color = MaterialTheme.colors.onPrimary,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                    }
                }
                BackHandler {
                    hideMenu()
                }
            }
        }
    }
}

private fun onClickMainMenuItem(
    menu: Menu,
    contentViewModel: ContentViewModel,
    showAudioPlayer: () -> Unit,
    navigate: (String) -> Unit,
    openFindInPageState: MutableState<Boolean>
) {
    when (menu) {
        Menu.TOP -> {
            contentViewModel.toTop()
        }
        Menu.BOTTOM -> {
            contentViewModel.toBottom()
        }
        Menu.SHARE -> {
            contentViewModel?.share()
        }
        Menu.CODE_READER -> {
            navigate(
                "tool/barcode_reader"
            )
        }
        Menu.LOAN_CALCULATOR -> {
            navigate(
                "tool/loan"
            )
        }
        Menu.RSS_READER -> {
            navigate(
                "tool/rss/list"
            )
        }
        Menu.NUMBER_PLACE -> {
            navigate(
                "tool/number/place"
            )
        }
        Menu.AUDIO -> {
            showAudioPlayer()
        }
        Menu.BOOKMARK -> {
            navigate(
                "web/bookmark/list"
            )
        }
        Menu.VIEW_HISTORY -> {
            navigate(
                "web/history/list"
            )
        }
        Menu.IMAGE_VIEWER -> {
            navigate(
                "tool/image/list"
            )
        }
        Menu.CALENDAR -> {
            contentViewModel?.openCalendar()
        }
        Menu.WEB_SEARCH -> {
            contentViewModel?.webSearch()
        }
        Menu.ABOUT_THIS_APP -> {
            navigate("about")
        }
        Menu.TODO_TASKS_BOARD -> {
            navigate(
                "tool/task/board"
            )
        }
        Menu.TODO_TASKS -> {
            navigate(
                "tool/task/list"
            )
        }
        Menu.VIEW_ARCHIVE -> {
            navigate(
                "web/archive/list"
            )
        }
        Menu.FIND_IN_PAGE -> {
            openFindInPageState.value = true
        }
    }
}