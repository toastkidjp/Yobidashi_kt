/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.rss.view

import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.rss.R
import jp.toastkid.rss.api.RssReaderApi
import jp.toastkid.rss.model.Item
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * TODO implement setting with compose.
 */
@Composable
fun RssReaderListUi() {
    val context = LocalContext.current
    val items = remember { mutableStateListOf<Item>() }

    LaunchedEffect(key1 = "", block = {
        CoroutineScope(Dispatchers.IO).launch {
            items.clear()

            val readRssReaderTargets = PreferenceApplier(context).readRssReaderTargets()
            readRssReaderTargets.asFlow()
                .mapNotNull { RssReaderApi().invoke(it) }
                .collect {
                    withContext(Dispatchers.Main) {
                        items.addAll(it.items)
                    }
                }
        }
    })

    RssReaderList(items)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RssReaderList(items: List<Item>) {
    val activity = LocalContext.current as? ComponentActivity ?: return
    val browserViewModel = ViewModelProvider(activity)
        .get(BrowserViewModel::class.java)

    val listState = rememberLazyListState()

    LazyColumn(state = listState) {
        items(items) {
            Surface(
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 2.dp,
                        bottom = 2.dp
                    )
                    .combinedClickable(
                        enabled = true,
                        onClick = {
                            browserViewModel.open(it.link.toUri())
                        },
                        onLongClick = {
                            browserViewModel.openBackground(it.link.toUri())
                        }
                    ),
                elevation = 4.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    AsyncImage(
                        R.drawable.ic_rss_feed,
                        contentDescription = stringResource(id = R.string.image),
                        modifier = Modifier.width(32.dp),
                        colorFilter = ColorFilter.tint(
                            colorResource(id = R.color.colorPrimary),
                            BlendMode.SrcIn
                        )
                    )
                    Column {
                        Text(
                            text = it.title,
                            fontSize = 18.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                        )
                        Text(
                            text = it.link,
                            color = colorResource(R.color.link_blue),
                            fontSize = 12.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                        Text(
                            text = it.content.toString(),
                            fontSize = 14.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 3,
                        )
                        Text(
                            text = it.source,
                            fontSize = 12.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                        )
                        Text(
                            text = it.date,
                            color = colorResource(R.color.darkgray_scale),
                            fontSize = 12.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
