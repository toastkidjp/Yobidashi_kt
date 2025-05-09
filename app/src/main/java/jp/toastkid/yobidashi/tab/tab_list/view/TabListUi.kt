/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.tab.tab_list.view

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.End
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Start
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.input.Inputs
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.ui.image.EfficientImage
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
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TabListUi(tabAdapter: TabAdapter, modifier: Modifier = Modifier) {
    val context = LocalContext.current as? ComponentActivity ?: return
    val contentViewModel = viewModel(ContentViewModel::class.java, context)
    val coroutineScope = rememberCoroutineScope()

    val tabs = remember { mutableStateListOf<Tab>() }
    val runOpenTab = remember { AtomicBoolean(false) }

    LaunchedEffect(key1 = context) {
        (0 until tabAdapter.size()).forEach {
            val tab = tabAdapter.getTabByIndex(it) ?: return@forEach
            tabs.add(tab)
        }
    }

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

    val initialIndex = remember { tabAdapter.currentTabId() }
    val deletedTabIds = remember { mutableStateListOf<String>() }

    val localView = LocalView.current

    ModalBottomSheet(
        onDismissRequest = { contentViewModel.switchTabList() },
        tonalElevation = 1.dp,
        containerColor = MaterialTheme.colorScheme.primary,
    ) {
        Box(modifier) {
            EfficientImage(
                model = PreferenceApplier(context).backgroundImagePath,
                contentDescription = stringResource(id = R.string.content_description_background),
                alignment = Alignment.Center,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
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
                    val tabThumbnails = TabThumbnails.with(context)

                    itemsIndexed(tabs, { _, tab -> tab.id() }) { position, tab ->
                        val backgroundColor = if (currentIndex == position)
                            Color(
                                ColorUtils.setAlphaComponent(
                                    MaterialTheme.colorScheme.secondary.toArgb(),
                                    128
                                )
                            )
                        else
                            Color.Transparent

                        ReorderableItem(state, key = tab.id(), defaultDraggingModifier = Modifier.animateItem()) { _ ->
                            TabItem(
                                tab,
                                tabThumbnails.assignNewFile(tab.thumbnailPath()),
                                backgroundColor,
                                visibility = {
                                    deletedTabIds.contains(it.id()).not()
                                },
                                onClick = {
                                    tabAdapter.replace(tab)
                                    closeOnly(coroutineScope, contentViewModel)
                                },
                                onDelete = {
                                    deletedTabIds.add(tab.id())
                                    tabAdapter.closeTab(tabAdapter.indexOf(tab))
                                    tabs.remove(tab)
                                }
                            )
                        }
                    }
                }

                val tint = MaterialTheme.colorScheme.onPrimary
                val backgroundColor = MaterialTheme.colorScheme.primary
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
                        jp.toastkid.lib.R.drawable.ic_article,
                        R.string.title_article_viewer,
                        tint,
                        backgroundColor,
                        Modifier.padding(4.dp)
                    ) {
                        contentViewModel.openArticleList()
                        closeOnly(coroutineScope, contentViewModel)
                    }

                    TabActionFab(
                        R.drawable.ic_web,
                        R.string.title_browser,
                        tint,
                        backgroundColor,
                        Modifier.padding(4.dp)
                    ) {
                        contentViewModel.open(PreferenceApplier(context).homeUrl.toUri())
                        closeOnly(coroutineScope, contentViewModel)
                    }
                    TabActionFab(
                        R.drawable.ic_add_tab,
                        jp.toastkid.lib.R.string.open,
                        tint,
                        backgroundColor,
                        Modifier.padding(4.dp)
                    ) {
                        // For suppressing replace screen.
                        closeOnly(coroutineScope, contentViewModel)
                        runOpenTab.set(true)
                    }
                }
            }
        }

        LaunchedEffect(contentViewModel.showTabList()) {
            Inputs().hideKeyboard(localView)
        }
    }

    DisposableEffect(tabAdapter) {
        onDispose {
            if (initialIndex != tabAdapter.currentTabId()) {
                contentViewModel.replaceToCurrentTab()
            }

            if (runOpenTab.get()) {
                contentViewModel.openNewTab()
            }

            tabAdapter.saveTabList()
        }
    }
}

private fun closeOnly(
    coroutineScope: CoroutineScope,
    contentViewModel: ContentViewModel
) {
    coroutineScope.launch {
        contentViewModel.switchTabList()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TabItem(
    tab: Tab,
    thumbnail: File,
    backgroundColor: Color,
    visibility: (Tab) -> Boolean,
    onClick: (Tab) -> Unit,
    onDelete: (Tab) -> Unit
) {
    val dismissSnackbarDistance = with(LocalDensity.current) { -360.dp.toPx() }
    val anchors = DraggableAnchors {
        Start at 0f
        End at dismissSnackbarDistance
    }
    val swipeableState = remember {
        AnchoredDraggableState(
            initialValue = Start,
            anchors = anchors,
            positionalThreshold = { dismissSnackbarDistance },
            velocityThreshold = { 3000000.dp.value },
            snapAnimationSpec = spring(),
            decayAnimationSpec = exponentialDecay(),
            confirmValueChange = {
                if (it == End) {
                    onDelete(tab)
                }
                true
            }
        )
    }

    AnimatedVisibility(
        visibility(tab),
        enter = slideInVertically(initialOffsetY = { it }),
        exit = shrinkHorizontally(shrinkTowards = Alignment.Start)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(120.dp)
                .height(160.dp)
                .clickable {
                    onClick(tab)
                }
                .drawBehind { drawRect(backgroundColor) }
                .offset {
                    IntOffset(
                        0,
                        swipeableState
                            .offset
                            .roundToInt()
                    )
                }
                .anchoredDraggable(
                    state = swipeableState,
                    orientation = Orientation.Vertical
                )
        ) {
            Surface(
                shadowElevation = 4.dp,
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
                    EfficientImage(
                        model = thumbnail,
                        contentDescription = tab.title(),
                        contentScale = ContentScale.FillHeight,
                        placeholder = painterResource(id = R.drawable.ic_yobidashi),
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .align(Alignment.TopCenter)
                    )

                    val primaryColor = MaterialTheme.colorScheme.primary
                    Text(
                        text = tab.title(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 2,
                        fontSize = 14.sp,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .drawBehind { drawRect(primaryColor) }
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
        containerColor = buttonColor,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painterResource(id = iconId),
            stringResource(id = contentDescriptionId),
            tint = iconColor
        )
    }
}
