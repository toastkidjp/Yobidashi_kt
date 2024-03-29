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
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.compat.material3.FractionalThreshold
import jp.toastkid.lib.compat.material3.ResistanceConfig
import jp.toastkid.lib.compat.material3.SwipeableState
import jp.toastkid.lib.compat.material3.swipeableCompat
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.network.NetworkChecker
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.ui.menu.view.OptionMenuItem
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.main.ui.finder.FindInPage
import jp.toastkid.yobidashi.wikipedia.random.RandomWikipedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun AppBar() {
    val activity = LocalContext.current as? ComponentActivity ?: return
    val contentViewModel = viewModel(ContentViewModel::class.java, activity)

    val sizePx = with(LocalDensity.current) { 72.dp.toPx() }
    val anchors = mapOf(-sizePx to 1, 0f to 0)
    val verticalState = remember { mutableStateOf(0) }
    val swipeableState = SwipeableState(
        initialValue = verticalState.value,
        confirmStateChange = {
            if (it == 1) {
                verticalState.value = 1
                contentViewModel.switchTabList()
                CoroutineScope(Dispatchers.IO).launch {
                    delay(400L)
                    verticalState.value = 0
                }
            }
            true
        }
    )

    val widthPx = with(LocalDensity.current) { 72.dp.toPx() }
    val horizontalAnchors = mapOf(0f to 0, widthPx to 1, -widthPx to 2)
    val stateValue = remember { mutableStateOf(0) }
    val horizontalSwipeableState = SwipeableState(
        initialValue = stateValue.value,
        confirmStateChange = {
            if (it == 1) {
                stateValue.value = 1
                contentViewModel.previousTab()
                stateValue.value = 0
            } else if (it == 2) {
                stateValue.value = 2
                contentViewModel.nextTab()
                stateValue.value = 0
            }
            true
        }
    )

    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.primary,
        tonalElevation = 4.dp,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
        modifier = Modifier
            .height(72.dp)
            .offset {
                IntOffset(
                    x = 0,
                    y = -1 * contentViewModel.bottomBarOffsetHeightPx.value.roundToInt()
                )
            }
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .swipeableCompat(
                    state = horizontalSwipeableState,
                    anchors = horizontalAnchors,
                    thresholds = { _, _ -> FractionalThreshold(0.75f) },
                    resistance = ResistanceConfig(0.5f),
                    orientation = Orientation.Horizontal
                )
                .swipeableCompat(
                    state = swipeableState,
                    anchors = anchors,
                    thresholds = { _, _ -> FractionalThreshold(0.75f) },
                    resistance = ResistanceConfig(0.5f),
                    orientation = Orientation.Vertical
                )
                .offset {
                    IntOffset(
                        horizontalSwipeableState.offset.value.toInt(),
                        swipeableState.offset.value.toInt()
                    )
                }
        ) {
            if (contentViewModel.openFindInPageState.value) {
                FindInPage()
            } else {
                contentViewModel.appBarContent.value()
            }
        }

        OverflowMenu(
            contentViewModel.optionMenus,
            { contentViewModel.switchTabList() }
        ) { activity.finish() }
    }

}

@Composable
private fun OverflowMenu(
    menus: List<OptionMenu>,
    switchTabList: () -> Unit,
    finishApp: () -> Unit
) {
    val openOptionMenu = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val contentViewModel = (context as? ViewModelStoreOwner)?.let {
        viewModel(ContentViewModel::class.java, context)
    }

    Box(modifier = Modifier
        .width(32.dp)
        .fillMaxHeight()
        .clickable { openOptionMenu.value = true }) {
        Icon(
            painterResource(id = R.drawable.ic_option_menu),
            contentDescription = stringResource(id = R.string.title_option_menu),
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.Center)
        )

        val commonOptionMenuItems = listOf(
            OptionMenu(
                titleId = R.string.reset_button_position,
                action = {
                    PreferenceApplier(context).clearMenuFabPosition()
                    contentViewModel?.resetMenuFabPosition()
                }),
            OptionMenu(titleId = R.string.menu_random_wikipedia, action = {
                if (PreferenceApplier(context).wifiOnly &&
                    NetworkChecker().isUnavailableWiFi(context)
                ) {
                    contentViewModel?.snackShort(jp.toastkid.web.R.string.message_wifi_not_connecting)
                    return@OptionMenu
                }

                RandomWikipedia()
                    .fetchWithAction { title, link ->
                        contentViewModel?.open(link)
                        contentViewModel?.snackShort(
                            context.getString(
                                R.string.message_open_random_wikipedia,
                                title
                            )
                        )
                    }
            }),
            OptionMenu(
                titleId = R.string.title_tab_list,
                action = switchTabList),
            OptionMenu(
                titleId = R.string.title_settings,
                action = { contentViewModel?.nextRoute("setting/top") }),
            OptionMenu(titleId = R.string.exit, action = finishApp)
        )
        val optionMenuItems =
            menus.union(commonOptionMenuItems).distinct()

        DropdownMenu(
            expanded = openOptionMenu.value,
            onDismissRequest = { openOptionMenu.value = false }
        ) {
            optionMenuItems.forEach {
                DropdownMenuItem(
                    text = {
                        OptionMenuItem(it)
                    },
                    onClick = {
                        openOptionMenu.value = false
                        it.action()
                    }
                )
            }
        }
    }
}
