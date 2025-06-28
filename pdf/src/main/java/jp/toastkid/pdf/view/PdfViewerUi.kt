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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.view.scroll.StateScrollerFactory
import jp.toastkid.pdf.PdfImageFactory
import jp.toastkid.ui.image.EfficientImage
import jp.toastkid.ui.modifier.onTransform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun PdfViewerUi(uri: Uri, modifier: Modifier) {
    val context = LocalContext.current as? ComponentActivity ?: return
    val images = remember { mutableStateListOf<Bitmap>() }

    val listState = rememberPagerState { images.size }

    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        val contentViewModel = ViewModelProvider(context).get(ContentViewModel::class.java)
        contentViewModel.replaceAppBarContent { AppBarUi(listState.pageCount, {
            coroutineScope.launch {
                listState.animateScrollToPage(it)
            }
        }) }
        withContext(Dispatchers.IO) {
            contentViewModel?.receiveEvent(StateScrollerFactory().invoke(listState))
        }
    }

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

    VerticalPager (
        listState,
        beyondViewportPageCount = 1,
        pageSize = PageSize.Fill,
        modifier = modifier
    ) {
        val bitmap = images[it]
        Surface(
            shadowElevation = 4.dp,
            color = Color(0xFFF0F0F0),
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .padding(vertical = 4.dp)
        ) {
            var scale by remember { mutableStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .pointerInput(bitmap) {
                        detectTapGestures(
                            onPress = { /* Called when the gesture starts */ },
                            onDoubleTap = {
                                scale = 1f
                                offset = Offset.Zero
                            },
                            onLongPress = { },
                            onTap = { /* Called on Tap */ }
                        )
                    }
            ) {
                val max = images.size
                EfficientImage(
                    model = bitmap,
                    contentDescription = "${it + 1} / $max",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y,
                        )
                        .pointerInput(Unit) {
                            onTransform(
                                onGesture = { offsetChange, zoomChange, _ ->
                                    scale *= zoomChange
                                    offset += offsetChange
                                },
                                onPointerInputChange = { it, _ ->
                                    if (it.positionChanged()) {
                                        it.consume()
                                    }
                                }
                            )
                        }
                        .align(Alignment.Center)
                )
                Text(
                    text = "${it + 1} / $max",
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
    }
}

@Composable
private fun AppBarUi(pageSize: Int, onValueChange: (Int) -> Unit) {
    var sliderPosition by remember { mutableStateOf(0f) }
    if (pageSize == 0) {
        return
    }

    Slider(
        value = sliderPosition,
        onValueChange = {
            sliderPosition = it
            onValueChange(it.roundToInt())
        },
        valueRange = (0f..(pageSize - 1).toFloat()),
        steps = max(1, pageSize - 2),
        colors = SliderDefaults.colors().copy(
            activeTrackColor = MaterialTheme.colorScheme.secondary
        )
    )
}
