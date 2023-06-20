/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.pdf.view

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.view.scroll.usecase.ScrollerUseCase
import jp.toastkid.pdf.PdfImageFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import kotlin.math.roundToInt

@Composable
fun PdfViewerUi(uri: Uri) {
    val context = LocalContext.current as? ComponentActivity ?: return

    val listState = rememberLazyListState()

    val viewModelProvider = ViewModelProvider(context)
    val contentViewModel = viewModelProvider.get(ContentViewModel::class.java)
    contentViewModel.replaceAppBarContent { AppBarUi(listState) }

    ScrollerUseCase(
        contentViewModel,
        listState
    ).invoke(LocalLifecycleOwner.current)

    PdfPageList(uri, listState)
}

@Composable
private fun PdfPageList(uri: Uri, listState: LazyListState) {
    val context = LocalContext.current

    val pdfRenderer =
        remember {
            try {
                context.contentResolver.openFileDescriptor(uri, "r")
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                return@remember null
            }
                ?.let { PdfRenderer(it) }
        }
            ?: return

    val images = remember { mutableStateListOf<Bitmap>() }
    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            val pdfImageFactory = PdfImageFactory()
            images.addAll(
                (0 until pdfRenderer.pageCount).map {
                    pdfImageFactory.invoke(pdfRenderer.openPage(it))
                }
            )
        }
    }

    val max = images.size
    LazyColumn(state = listState) {
        itemsIndexed(images) { index, bitmap ->
            Surface(
                shadowElevation = 4.dp,
                color = Color(0xFFF0F0F0),
                modifier = Modifier
                    .padding(
                        start = 8.dp,
                        end = 8.dp,
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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(state = state)
                ) {
                    AsyncImage(
                        model = bitmap,
                        contentDescription = "${index + 1} / $max"
                    )
                    Text(
                        text = "${index + 1} / $max",
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
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
                    (scrollState.layoutInfo.totalItemsCount * it).roundToInt(),
                    0
                )
            }
        },
        steps = scrollState.layoutInfo.totalItemsCount - 1
    )
}
