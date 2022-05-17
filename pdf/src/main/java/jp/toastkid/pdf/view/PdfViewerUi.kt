/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.pdf.view

import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.view.scroll.usecase.ScrollerUseCase
import jp.toastkid.pdf.PdfImageFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PdfViewerUi(uri: Uri) {
    val context = LocalContext.current as? ComponentActivity ?: return

    val listState = rememberLazyListState()

    val pdfRenderer =
        context.contentResolver.openFileDescriptor(uri, "r")
            ?.let { PdfRenderer(it) }
            ?: return

    val viewModelProvider = ViewModelProvider(context)
    viewModelProvider.get(AppBarViewModel::class.java).replace { AppBarUi(listState) }

    ScrollerUseCase(
        viewModelProvider.get(ContentViewModel::class.java),
        listState
    ).invoke(context)

    PdfPageList(pdfRenderer, listState)
}

@Composable
private fun PdfPageList(pdfRenderer: PdfRenderer, listState: LazyListState) {
    val pdfImageFactory = PdfImageFactory()

    MaterialTheme {
        LazyColumn(state = listState) {
            val max = pdfRenderer.pageCount
            items(max) {
                Surface(
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 2.dp,
                            bottom = 2.dp
                        )
                ) {
                    var scale by remember { mutableStateOf(1f) }
                    var offset by remember { mutableStateOf(Offset.Zero) }

                    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
                        scale *= zoomChange
                        offset += offsetChange
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                            .transformable(state = state)
                    ) {
                        AsyncImage(
                            model = pdfImageFactory.invoke(pdfRenderer.openPage(it)),
                            contentDescription = "${it + 1} / $max"
                        )
                        Text(
                            text = "${it + 1} / $max",
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppBarUi(scrollState: LazyListState) {
    var sliderPosition by remember { mutableStateOf(0f) }
    if (scrollState.layoutInfo.totalItemsCount == 0) {
        return
    }
    Slider(
        value = sliderPosition,
        onValueChange = {
            sliderPosition = it
            CoroutineScope(Dispatchers.Main).launch {
                scrollState.scrollToItem(
                    ((scrollState.layoutInfo.totalItemsCount ?: 0 ) * it).roundToInt(),
                    0
                )
            }
        },
        steps = (scrollState.layoutInfo.totalItemsCount ?: 1) - 1
    )
}

/*
CoroutineScope(Dispatchers.Main).launch {
        scrollState?.scrollToItem(0, 0)
    }
    CoroutineScope(Dispatchers.Main).launch {
        scrollState?.scrollToItem(scrollState?.layoutInfo?.totalItemsCount ?: 0, 0)
    }
 */