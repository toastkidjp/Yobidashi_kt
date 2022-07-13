/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.BottomAppBar
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.ui.menu.view.OptionMenuItem
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.main.ui.finder.FindInPage
import kotlin.math.roundToInt

@Composable
internal fun AppBar(
    openFindInPageState: MutableState<Boolean>,
    openSetting: () -> Unit
) {
    val activity = LocalContext.current as? ComponentActivity ?: return
    val contentViewModel = viewModel(ContentViewModel::class.java, activity)
    val pageSearcherInput = remember { mutableStateOf("") }

    BottomAppBar(
        backgroundColor = MaterialTheme.colors.primary,
        elevation = 4.dp,
        modifier = Modifier
            .height(72.dp)
            .offset {
                IntOffset(
                    x = 0,
                    y = -1 * contentViewModel.bottomBarOffsetHeightPx.value.roundToInt()
                )
            }
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (openFindInPageState.value) {
                FindInPage(
                    openFindInPageState,
                    MaterialTheme.colors.onPrimary,
                    pageSearcherInput
                )
            } else {
                contentViewModel.appBarContent.value()
            }
        }

        OverflowMenu(
            MaterialTheme.colors.onPrimary,
            contentViewModel.optionMenus,
            { contentViewModel.switchTabList() },
            openSetting,
            { activity.finish() }
        )
    }
}

@Composable
private fun OverflowMenu(
    tint: Color,
    menus: List<OptionMenu>,
    switchTabList: () -> Unit,
    openSetting: () -> Unit,
    finishApp: () -> Unit
) {
    val openOptionMenu = remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .width(32.dp)
        .clickable { openOptionMenu.value = true }) {
        Icon(
            painterResource(id = R.drawable.ic_option_menu),
            contentDescription = stringResource(id = R.string.title_option_menu),
            tint = tint
        )

        val commonOptionMenuItems = listOf(
            OptionMenu(
                titleId = R.string.title_tab_list,
                action = switchTabList),
            OptionMenu(
                titleId = R.string.title_settings,
                action = openSetting),
            OptionMenu(titleId = R.string.exit, action = finishApp)
        )
        val optionMenuItems =
            menus?.union(commonOptionMenuItems)?.distinct()

        DropdownMenu(
            expanded = openOptionMenu.value,
            onDismissRequest = { openOptionMenu.value = false }) {
            optionMenuItems.forEach {
                DropdownMenuItem(onClick = {
                    openOptionMenu.value = false
                    it.action()
                }) {
                    OptionMenuItem(it)
                }
            }
        }
    }
}
