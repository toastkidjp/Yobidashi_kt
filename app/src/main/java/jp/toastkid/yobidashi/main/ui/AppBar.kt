/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main.ui

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.Arrangement.Absolute.Center
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AppBar() {
    val activity = LocalContext.current as? ComponentActivity ?: return
    val contentViewModel = viewModel(ContentViewModel::class.java, activity)

    val sizePx = with(LocalDensity.current) { 72.dp.toPx() }
    val anchors = DraggableAnchors {
        "Start" at 0f
        "End" at -sizePx
    }
    val swipeableState = remember {
        AnchoredDraggableState(
            initialValue = "Start",
            anchors = anchors,
            positionalThreshold = { sizePx * 0.5f },
            velocityThreshold = { 120.dp.value },
            animationSpec = spring(),
            confirmValueChange = {
                true
            }
        )
    }

    val widthPx = with(LocalDensity.current) { 72.dp.toPx() }
    val horizontalAnchors = DraggableAnchors {
        AnimatedContentTransitionScope.SlideDirection.Start at widthPx
        Center at 0f
        AnimatedContentTransitionScope.SlideDirection.End at -widthPx
    }
    val horizontalSwipeableState = remember {
        AnchoredDraggableState(
            initialValue = Center,
            anchors = horizontalAnchors,
            positionalThreshold = { widthPx * 0.75f },
            velocityThreshold = { 300.dp.value },
            animationSpec = spring(),
            confirmValueChange = {
                true
            }
        )
    }

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(horizontalSwipeableState.currentValue) {
        if (horizontalSwipeableState.currentValue == AnimatedContentTransitionScope.SlideDirection.Start) {
            contentViewModel.previousTab()
        } else if (horizontalSwipeableState.currentValue == AnimatedContentTransitionScope.SlideDirection.End) {
            contentViewModel.nextTab()
        }
        coroutineScope.launch {
            horizontalSwipeableState.snapTo(Center)
        }
    }
    LaunchedEffect(swipeableState.currentValue) {
        if (swipeableState.currentValue == "End") {
            contentViewModel.switchTabList()
            CoroutineScope(Dispatchers.IO).launch {
                delay(400L)
                swipeableState.snapTo("Start")
            }
        }
    }
    SideEffect {
        if (!horizontalSwipeableState.isAnimationRunning) {
            coroutineScope.launch { horizontalSwipeableState.snapTo(Center) }
        }
    }

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
                .anchoredDraggable(
                    state = horizontalSwipeableState,
                    orientation = Orientation.Horizontal
                )
                .anchoredDraggable(
                    state = swipeableState,
                    orientation = Orientation.Vertical
                )
                .offset {
                    IntOffset(
                        horizontalSwipeableState
                            .requireOffset()
                            .toInt(),
                        swipeableState
                            .requireOffset()
                            .toInt()
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
            painterResource(id = jp.toastkid.lib.R.drawable.ic_option_menu),
            contentDescription = stringResource(id = jp.toastkid.lib.R.string.title_option_menu),
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.Center)
        )

        DropdownMenu(
            expanded = openOptionMenu.value,
            onDismissRequest = { openOptionMenu.value = false }
        ) {
            menus.union(
                listOf(
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
                            contentViewModel?.snackShort(jp.toastkid.lib.R.string.message_wifi_not_connecting)
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
                        titleId = R.string.action_settings,
                        action = { contentViewModel?.nextRoute("setting/top") }),
                    OptionMenu(titleId = R.string.exit, action = finishApp)
                )
            ).distinct().forEach {
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
