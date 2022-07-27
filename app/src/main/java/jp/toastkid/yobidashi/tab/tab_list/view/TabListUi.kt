/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.tab.tab_list.view

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ResistanceConfig
import androidx.compose.material.Surface
import androidx.compose.material.SwipeableState
import androidx.compose.material.Text
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.TabListViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.tab.TabAdapter
import jp.toastkid.yobidashi.tab.TabThumbnails
import jp.toastkid.yobidashi.tab.model.Tab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
internal fun TabListUi(tabAdapter: TabAdapter) {
    val context = LocalContext.current as? ComponentActivity ?: return
    val preferenceApplier = PreferenceApplier(context)
    val tabThumbnails = TabThumbnails.with(LocalContext.current)
    val contentViewModel = viewModel(ContentViewModel::class.java, context)
    val tabListViewModel = viewModel(TabListViewModel::class.java, context)
    val coroutineScope = rememberCoroutineScope()

    val tabs = remember { mutableStateListOf<Tab>() }
    refresh(tabAdapter, tabs)

    val state =  rememberReorderableLazyListState(
        onMove = { from, to ->
            tabs.add(to.index, tabs.removeAt(from.index))
            tabAdapter.swap(to.index, from.index)
        },
        onDragEnd = { _, _ ->
            tabAdapter.saveTabList()
        },
        listState = rememberLazyListState(max(0, tabAdapter.index() - 1))
    )

    val sizePx = with(LocalDensity.current) { dimensionResource(R.dimen.tab_list_item_height).toPx() }
    val anchors = mapOf(0f to 0, -sizePx to 1)

    val initialIndex = tabAdapter.currentTabId()
    val deletedTabIds = remember { mutableStateListOf<String>() }

    Box {
        AsyncImage(
            model = preferenceApplier.backgroundImagePath,
            contentDescription = stringResource(id = R.string.content_description_background),
            alignment = Alignment.Center,
            contentScale = ContentScale.Crop,
            modifier = Modifier.heightIn(max = 236.dp)
        )

        Column {
            LazyRow(
                state = state.listState,
                contentPadding = PaddingValues(horizontal = 4.dp),
                modifier = Modifier
                    .reorderable(state)
                    .detectReorderAfterLongPress(state)
            ) {
                val currentIndex = tabAdapter.index()

                itemsIndexed(tabs) { position, tab ->
                    val backgroundColor = if (currentIndex == position)
                        Color(
                            ColorUtils.setAlphaComponent(
                                MaterialTheme.colors.primary.toArgb(),
                                128
                            )
                        )
                    else
                        Color.Transparent

                    ReorderableItem(state, key = tab, defaultDraggingModifier = Modifier) { isDragging ->
                        TabItem(
                            tab,
                            tabThumbnails.assignNewFile(tab.thumbnailPath()),
                            backgroundColor,
                            anchors,
                            visibility = {
                                deletedTabIds.contains(it.id()).not()
                            },
                            onClick = {
                                tabAdapter.replace(tab)
                                closeOnly(coroutineScope, contentViewModel)
                            },
                            onClose = {
                                deletedTabIds.add(tab.id())
                                tabAdapter.closeTab(tabAdapter.indexOf(tab))
                            }
                        )
                    }
                }
            }

            val tint = Color(preferenceApplier.fontColor)
            val backgroundColor = Color(preferenceApplier.color)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                TabActionFab(
                    R.drawable.ic_edit,
                    R.string.title_editor,
                    tint,
                    backgroundColor,
                    Modifier.padding(4.dp)
                ) {
                    contentViewModel.openEditorTab()
                    closeOnly(coroutineScope, contentViewModel)
                }
                TabActionFab(
                    R.drawable.ic_pdf,
                    R.string.title_open_pdf,
                    tint,
                    backgroundColor,
                    Modifier.padding(4.dp)
                ) {
                    contentViewModel.openPdf()
                    closeOnly(coroutineScope, contentViewModel)
                }
                TabActionFab(
                    R.drawable.ic_article,
                    R.string.title_article_viewer,
                    tint,
                    backgroundColor,
                    Modifier.padding(4.dp)
                ) {
                    contentViewModel.openArticleList()
                    closeOnly(coroutineScope, contentViewModel)
                }
                val browserViewModel = viewModel(BrowserViewModel::class.java, context)
                TabActionFab(
                    R.drawable.ic_web,
                    R.string.title_browser,
                    tint,
                    backgroundColor,
                    Modifier.padding(4.dp)
                ) {
                    browserViewModel.open(preferenceApplier.homeUrl.toUri())
                    closeOnly(coroutineScope, contentViewModel)
                }
                TabActionFab(
                    R.drawable.ic_add_tab,
                    R.string.open,
                    tint,
                    backgroundColor,
                    Modifier.padding(4.dp)
                ) {
                    // For suppressing replace screen.
                    contentViewModel.setHideBottomSheetAction {  }
                    tabListViewModel.openNewTab()
                    closeOnly(coroutineScope, contentViewModel)
                }
            }
        }
    }

    LaunchedEffect(key1 = initialIndex, block = {
        contentViewModel.setHideBottomSheetAction {
            if (initialIndex != tabAdapter.currentTabId()) {
                contentViewModel.replaceToCurrentTab()
            }

            contentViewModel.setHideBottomSheetAction {  }
        }
    })

    DisposableEffect(tabAdapter) {
        onDispose {
            tabAdapter.saveTabList()
        }
    }
}

private fun closeOnly(
    coroutineScope: CoroutineScope,
    contentViewModel: ContentViewModel
) {
    coroutineScope.launch {
        contentViewModel.hideBottomSheet()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TabItem(
    tab: Tab,
    thumbnail: File,
    backgroundColor: Color,
    anchors: Map<Float, Int>,
    visibility: (Tab) -> Boolean,
    onClick: (Tab) -> Unit,
    onClose: (Tab) -> Unit
) {
    val swipeableState = SwipeableState(
        initialValue = 0,
        confirmStateChange = {
            if (it == 1) {
                onClose(tab)
            }
            true
        }
    )

    AnimatedVisibility(
        visibility(tab),
        enter = slideInVertically(initialOffsetY = { it }),
        exit = shrinkHorizontally(shrinkTowards = Alignment.Start)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(dimensionResource(id = R.dimen.tab_list_item_width))
                .height(dimensionResource(R.dimen.tab_list_item_height))
                .clickable {
                    onClick(tab)
                }
                .background(backgroundColor)
                .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) }
                .swipeable(
                    swipeableState,
                    anchors = anchors,
                    thresholds = { _, _ -> FractionalThreshold(0.75f) },
                    resistance = ResistanceConfig(0.5f),
                    orientation = Orientation.Vertical
                )
        ) {
            Surface(
                elevation = 4.dp,
                modifier = Modifier
                    .width(112.dp)
                    .height(152.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(112.dp)
                        .height(152.dp)
                        .padding(4.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    AsyncImage(
                        model = thumbnail,
                        contentDescription = tab.title(),
                        contentScale = ContentScale.FillHeight,
                        placeholder = painterResource(id = R.drawable.ic_yobidashi),
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .align(Alignment.TopCenter)
                    )
                    Text(
                        text = tab.title(),
                        color = MaterialTheme.colors.onPrimary,
                        maxLines = 2,
                        fontSize = 14.sp,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(MaterialTheme.colors.primary)
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TabActionFab(
    iconId: Int,
    contentDescriptionId: Int,
    iconColor: Color,
    buttonColor: Color,
    modifier: Modifier,
    action: () -> Unit
) {
    FloatingActionButton(
        onClick = action,
        backgroundColor = buttonColor,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painterResource(id = iconId),
            stringResource(id = contentDescriptionId),
            tint = iconColor
        )
    }
}

private fun refresh(callback: TabAdapter, tabs: SnapshotStateList<Tab>) {
    tabs.clear()

    (0 until callback.size()).forEach {
        val tab = callback.getTabByIndex(it) ?: return@forEach
        tabs.add(tab)
    }
}