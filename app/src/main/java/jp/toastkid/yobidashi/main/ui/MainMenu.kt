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
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.navigation.Screen
import jp.toastkid.yobidashi.menu.Menu

@Composable
internal fun MainMenu(
    navigate: (Screen) -> Unit,
    showAudioPlayer: () -> Unit,
    hideMenu: () -> Unit
) {
    val activity = LocalActivity.current as? ComponentActivity ?: return
    val contentViewModel = viewModel(ContentViewModel::class.java, activity)
    val menuCount = remember { Menu.entries.size }
    val tooBigCount = remember { Menu.entries.size * 10 }
    val enableChat = remember { PreferenceApplier(activity).chatApiKey()?.isNotBlank() ?: false }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyRow(
            state = contentViewModel.menuScrollState(),
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
        ) {
            items(tooBigCount) { longIndex ->
                val menu = Menu.entries[longIndex % menuCount]
                if (!enableChat && menu == Menu.CHAT) {
                    return@items
                }
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable {
                            onClickMainMenuItem(
                                menu,
                                contentViewModel,
                                showAudioPlayer,
                                navigate
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
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            stringResource(id = menu.titleId),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(top = 8.dp)
                        )
                    }
                }

            }
        }

        BackHandler {
            hideMenu()
        }
    }
}

private fun onClickMainMenuItem(
    menu: Menu,
    contentViewModel: ContentViewModel,
    showAudioPlayer: () -> Unit,
    navigate: (Screen) -> Unit
) {
    when (menu) {
        Menu.TOP -> {
            contentViewModel.toTop()
        }
        Menu.BOTTOM -> {
            contentViewModel.toBottom()
        }
        Menu.SHARE -> {
            contentViewModel.share()
        }
        Menu.CODE_READER -> {
            navigate(
                Screen.BarcodeReader
            )
        }
        Menu.LOAN_CALCULATOR -> {
            navigate(
                Screen.LoanCalculator
            )
        }
        Menu.CONVERTER_TOOL -> {
            navigate(Screen.ConverterTool)
        }
        Menu.RSS_READER -> {
            navigate(
                Screen.RssReader
            )
        }
        Menu.NUMBER_PLACE -> {
            navigate(
                Screen.NumberPlace
            )
        }
        Menu.AUDIO -> {
            showAudioPlayer()
        }
        Menu.BOOKMARK -> {
            navigate(
                Screen.WebBookmark
            )
        }
        Menu.VIEW_HISTORY -> {
            navigate(
                Screen.WebHistory
            )
        }
        Menu.IMAGE_VIEWER -> {
            navigate(
                Screen.ImageViewer
            )
        }
        Menu.CALENDAR -> {
            contentViewModel.openCalendar()
        }
        Menu.WEB_SEARCH -> {
            contentViewModel.webSearch()
        }
        Menu.ABOUT_THIS_APP -> {
            navigate(Screen.AboutThisApp)
        }
        Menu.TODO_TASKS_BOARD -> {
            navigate(
                Screen.TaskBoard
            )
        }
        Menu.TODO_TASKS -> {
            navigate(
                Screen.TaskList
            )
        }
        Menu.VIEW_ARCHIVE -> {
            navigate(
                Screen.WebArchive
            )
        }
        Menu.FIND_IN_PAGE -> {
            contentViewModel.openFindInPageState.value = true
        }
        Menu.CHAT -> {
            navigate(Screen.Chat)
        }
        Menu.SENSOR -> {
            navigate(Screen.Sensor)
        }
        Menu.WORLD_TIME -> {
            navigate(Screen.WorldTime)
        }
    }
}