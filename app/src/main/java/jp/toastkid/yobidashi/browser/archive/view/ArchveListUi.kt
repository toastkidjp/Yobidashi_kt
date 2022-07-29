/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.archive.view

import android.net.Uri
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.list.ListActionAttachment
import jp.toastkid.ui.list.SwipeToDismissItem
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.archive.Archive
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ArchiveListUi() {
    val activityContext = LocalContext.current as? ComponentActivity ?: return
    val makeNew = Archive.makeNew(LocalContext.current)
    val fullItems = makeNew.listFiles()

    val viewModelProvider = ViewModelProvider(activityContext)
    if (fullItems.isEmpty()) {
        viewModelProvider.get(ContentViewModel::class.java)
            .snackShort(R.string.message_empty_archives)
        return
    }

    val browserViewModel = viewModelProvider.get(BrowserViewModel::class.java)

    val preferenceApplier = PreferenceApplier(activityContext)

    val listState = rememberLazyListState()

    val items = remember { mutableStateListOf<File>() }
    items.addAll(fullItems)

    LazyColumn(state = listState) {
        items(items) { archiveFile ->
            SwipeToDismissItem(
                onClickDelete = {
                    try {
                        archiveFile.delete()
                        items.remove(archiveFile)
                    } catch (e: IOException) {
                        Timber.e(e)
                    }
                },
                dismissContent = {
                    Row(
                        Modifier
                            .padding(start = 16.dp, end = 16.dp)
                            .clickable {
                                browserViewModel.open(Uri.fromFile(archiveFile))
                            }) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_archive),
                            contentDescription = stringResource(id = R.string.image),
                            colorFilter = ColorFilter.tint(
                                Color(preferenceApplier.color),
                                BlendMode.SrcIn
                            ),
                            modifier = Modifier
                                .padding(4.dp)
                                .width(40.dp)
                                .fillMaxHeight()
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(start = 8.dp, end = 8.dp)
                        ) {
                            Text(
                                text = archiveFile.nameWithoutExtension, maxLines = 1, fontSize = 16.sp,
                                overflow = TextOverflow.Ellipsis
                            )

                            val time =
                                DateFormat.format("yyyyMMdd HH:mm:ss", archiveFile.lastModified())
                            val fileSize =
                                NumberFormat.getIntegerInstance(
                                    Locale.getDefault()).format(archiveFile.length() / 1024
                                )

                            Text(
                                text = "$time / $fileSize[KB]",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            )
        }
    }

    ListActionAttachment.make(activityContext)
        .invoke(
            listState,
            LocalLifecycleOwner.current,
            items,
            fullItems.toList()
        ) { item, word -> item.name.contains(word) }
}