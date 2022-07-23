/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.view

import android.text.format.DateFormat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import jp.toastkid.search.SearchCategory
import jp.toastkid.ui.list.SwipeToDismissItem
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.UrlItem
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.browser.history.ViewHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
internal fun SearchItemContent(
    query: String?,
    category: String?,
    onClick: (Boolean) -> Unit,
    onDelete: () -> Unit,
    time: Long = -1
) {
    if (query == null || category == null) {
        return
    }

    SwipeToDismissItem(
        onClickDelete = {
            CoroutineScope(Dispatchers.IO).launch {
                onDelete()
            }
        },
        dismissContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.combinedClickable(
                    true,
                    onClick = {
                        onClick(false)
                    },
                    onLongClick = {
                        onClick(true)
                    }
                )
            ) {
                AsyncImage(
                    SearchCategory.findByCategory(category).iconId,
                    contentDescription = category,
                    placeholder = painterResource(id = R.drawable.ic_history_black),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.Center,
                    modifier = Modifier
                        .width(32.dp)
                        .fillMaxHeight()
                        .padding(start = 8.dp)
                )
                Column(
                    Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = query,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 18.sp
                    )
                    if (time != -1L) {
                        Text(
                            text = dateFormat(time),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 12.sp,
                            color = colorResource(id = R.color.darkgray_scale)
                        )
                    }
                }
            }
        },
        modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 2.dp)
    )
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun BindItemContent(
    urlItem: UrlItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    val title = when (urlItem) {
        is Bookmark -> urlItem.title
        is ViewHistory -> urlItem.title
        else -> ""
    }
    val url = when (urlItem) {
        is Bookmark -> urlItem.url
        is ViewHistory -> urlItem.url
        else -> ""
    }
    val time = dateFormat(
        when (urlItem) {
            is Bookmark -> urlItem.lastViewed
            is ViewHistory -> urlItem.lastViewed
            else -> 0L
        }
    )

    val iconFile = when (urlItem) {
        is Bookmark -> File(urlItem.favicon)
        is ViewHistory -> File(urlItem.favicon)
        else -> null
    }

    SwipeToDismissItem(
        onClickDelete = {
            CoroutineScope(Dispatchers.IO).launch {
                onDelete()
            }
        },
        dismissContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.combinedClickable(
                    true,
                    onClick = onClick,
                    onLongClick = onLongClick
                )
            ) {
                AsyncImage(
                    iconFile,
                    contentDescription = urlItem.urlString(),
                    placeholder = painterResource(id = R.drawable.ic_history_black),
                    contentScale = ContentScale.Inside,
                    modifier = Modifier
                        .width(32.dp)
                        .fillMaxHeight()
                        .padding(start = 8.dp)
                )
                Column(
                    Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 18.sp
                    )
                    Text(
                        text = url,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.link_blue)
                    )
                    Text(
                        text = time,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.darkgray_scale)
                    )
                }
            }
        },
        modifier = Modifier.padding(
            start = 8.dp,
            end = 8.dp,
            top = 2.dp,
            bottom = 2.dp
        )
    )
}

@Composable
private fun dateFormat(timeInMillis: Long) = DateFormat.format(
    stringResource(R.string.date_format),
    timeInMillis
).toString()
