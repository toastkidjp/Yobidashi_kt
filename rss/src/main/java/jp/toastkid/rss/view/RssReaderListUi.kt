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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import jp.toastkid.api.rss.RssReaderApi
import jp.toastkid.api.rss.model.Item
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.scroll.StateScrollerFactory
import jp.toastkid.rss.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RssReaderListUi() {
    val items = remember { mutableStateListOf<Item>() }

    val activity = LocalContext.current as? ComponentActivity ?: return

    val listState = rememberLazyListState()

    LazyColumn(state = listState) {
        println("items ${items.size}")
        items(items, { it.title + it.link + it.source }) {
            Surface(
                modifier = Modifier
                    .padding(
                        start = 8.dp,
                        end = 8.dp,
                        top = 2.dp,
                        bottom = 2.dp
                    )
                    .combinedClickable(
                        enabled = true,
                        onClick = {
                            ViewModelProvider(activity)
                                .get(ContentViewModel::class.java)
                                .open(it.link.toUri())
                        },
                        onLongClick = {
                            ViewModelProvider(activity)
                                .get(ContentViewModel::class.java)
                                .openBackground(it.link.toUri())
                        }
                    )
                    .animateItem(),
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(4.dp)
                ) {
                    Row {
                        Icon(
                            painterResource(id = R.drawable.ic_rss_feed),
                            contentDescription = stringResource(id = jp.toastkid.lib.R.string.image),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(end = 4.dp)
                        )

                        Text(
                            text = it.title,
                            fontSize = 18.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                        )
                    }
                    Text(
                        text = it.link,
                        color = colorResource(jp.toastkid.lib.R.color.link_blue),
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
                        color = colorResource(jp.toastkid.lib.R.color.darkgray_scale),
                        fontSize = 12.sp,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                }
            }
        }
    }

    LaunchedEffect(LocalLifecycleOwner.current, block = {
        withContext(Dispatchers.IO) {
            ViewModelProvider(activity).get(ContentViewModel::class)
                .receiveEvent(
                    StateScrollerFactory().invoke(listState),
                    items,
                    items.toList(),
                    { item, word -> item.title.contains(word) || item.description.contains(word) }
                )
        }
        withContext(Dispatchers.IO) {
            items.clear()

            val rssReaderApi = RssReaderApi()
            PreferenceApplier(activity).readRssReaderTargets()
                .asFlow()
                .mapNotNull(rssReaderApi::invoke)
                .collect {
                    items.addAll(it.items)
                }
        }
    })
}
