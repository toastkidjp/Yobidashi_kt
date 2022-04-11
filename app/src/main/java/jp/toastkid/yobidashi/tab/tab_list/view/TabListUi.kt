/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.tab.tab_list.view

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ResistanceConfig
import androidx.compose.material.Surface
import androidx.compose.material.SwipeableState
import androidx.compose.material.Text
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.tab.TabThumbnails
import jp.toastkid.yobidashi.tab.model.Tab
import jp.toastkid.yobidashi.tab.tab_list.TabListDialogFragment
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun TabListUi() {
    val context = LocalContext.current as? ComponentActivity ?: return
    val callback = context as? TabListDialogFragment.Callback ?: return
    val preferenceApplier = PreferenceApplier(context)
    val colorPair = preferenceApplier.colorPair()
    val tabThumbnails = TabThumbnails.with(LocalContext.current)
    val contentViewModel = viewModel(ContentViewModel::class.java, context)
    val currentTabId = remember { contentViewModel.currentTabId }
    val currentIndex = callback?.tabIndexFromTabList() ?: 0
    val state = rememberLazyListState(max(0, currentIndex - 1))

    val tabs = remember { mutableStateListOf<Tab>() }
    refresh(callback, tabs)

    val sizePx = with(LocalDensity.current) { dimensionResource(R.dimen.tab_list_item_height).toPx() }
    val anchors = mapOf(0f to 0, -sizePx to 1)

    MaterialTheme {
        LazyRow(state = state, contentPadding = PaddingValues(horizontal = 4.dp)) {
            itemsIndexed(tabs) { position, tab ->
                val swipeableState = SwipeableState(initialValue = 0, confirmStateChange = {
                    if (it == 1) {
                        callback.closeTabFromTabList(callback.tabIndexOfFromTabList(tab))
                        refresh(callback, tabs)
                    }
                    true
                })
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(dimensionResource(id = R.dimen.tab_list_item_width))
                        .height(dimensionResource(R.dimen.tab_list_item_height))
                        .clickable {
                            callback?.replaceTabFromTabList(tab)
                            callback?.onCloseTabListDialogFragment(currentTabId.value)
                            callback?.onCloseOnly()
                        }
                        .background(
                            if (currentIndex == position)
                                Color(ColorUtils.setAlphaComponent(colorPair.bgColor(), 128))
                            else
                                Color.Transparent
                        )
                        .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) }
                        .swipeable(
                            swipeableState,
                            anchors = anchors,
                            thresholds = { _, _ -> FractionalThreshold(0.75f) },
                            resistance = ResistanceConfig(0.5f),
                            orientation = Orientation.Vertical
                        )
                ){
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
                                model = tabThumbnails.assignNewFile(tab.thumbnailPath()),
                                contentDescription = tab.title(),
                                contentScale = ContentScale.FillHeight,
                                placeholder = painterResource(id = R.drawable.ic_yobidashi),
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .align(Alignment.TopCenter)
                            )
                            Text(
                                text = tab.title(),
                                color = Color(colorPair.fontColor()),
                                maxLines = 2,
                                fontSize = 14.sp,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .background(Color(colorPair.bgColor()))
                                    .padding(4.dp)
                            )
                        }
                    }

                    Icon(
                        painterResource(id = R.drawable.ic_remove_circle),
                        tint = Color(colorPair.fontColor()),
                        contentDescription = stringResource(id = R.string.delete),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .clickable {
                                val removeIndex =
                                    callback?.tabIndexOfFromTabList(tab) ?: return@clickable
                                callback?.closeTabFromTabList(removeIndex)
                                refresh(callback, tabs)
                            }
                    )
                }
            }
        }
    }

    /*

    adapter = Adapter(activityContext, callback as Callback)
    adapter.setCurrentIndex(index)

    binding = DataBindingUtil.inflate(
            LayoutInflater.from(activityContext),
            LAYOUT_ID,
            null,
            false
    )

    binding.dialog = this

    initRecyclerView(binding.recyclerView)

    colorPair.applyTo(binding.addArticleTab)
    colorPair.applyTo(binding.addPdfTab)
    colorPair.applyTo(binding.addEditorTab)
    colorPair.applyTo(binding.addTab)
    colorPair.applyTo(binding.clearTabs)

    binding.recyclerView.layoutManager?.scrollToPosition(index - 1)
    binding.recyclerView.scheduleLayoutAnimation()
    if (firstLaunch) {
        Toaster.snackShort(
                binding.snackbarParent,
                R.string.message_tutorial_remove_tab,
                colorPair
        )
        firstLaunch = false
    }
    lastTabId = callback?.currentTabIdFromTabList() ?: ""

    BackgroundImageLoaderUseCase().invoke(
            binding.background,
            PreferenceApplier(activityContext).backgroundImagePath
    )
     */
}

private fun refresh(callback: TabListDialogFragment.Callback, tabs: SnapshotStateList<Tab>) {
    tabs.clear()

    (0 until (callback?.getTabAdapterSizeFromTabList() ?: 0)).forEach {
        val tab = callback?.getTabByIndexFromTabList(it) ?: return@forEach
        tabs.add(tab)
    }
}