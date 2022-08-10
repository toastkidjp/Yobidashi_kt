/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.image.preview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateRotateBy
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
import jp.toastkid.image.Image
import jp.toastkid.image.R
import jp.toastkid.image.factory.GifImageLoaderFactory
import jp.toastkid.lib.ContentViewModel
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
internal fun ImagePreviewUi(images: List<Image>, initialIndex: Int) {
    val imageLoader = GifImageLoaderFactory().invoke(LocalContext.current)

    var scale by remember { mutableStateOf(1f) }
    var rotationY by remember { mutableStateOf(0f) }
    var rotationZ by remember { mutableStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        scale *= zoomChange
        rotationZ += rotationChange
        offset += offsetChange
    }
    var alphaSliderPosition by remember { mutableStateOf(0f) }
    var contrastSliderPosition by remember { mutableStateOf(0f) }

    val openMenu = remember { mutableStateOf(false) }
    val colorFilterState = remember { mutableStateOf<ColorFilter?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val index = remember { mutableStateOf(initialIndex) }

    val image = images[index.value]

    Box {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image.path).crossfade(true).build(),
            imageLoader = imageLoader,
            contentDescription = image.name,
            colorFilter = colorFilterState.value,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    //TODO alpha = alphaSliderPosition,
                    scaleX = scale,
                    scaleY = scale,
                    rotationY = rotationY,
                    rotationZ = rotationZ
                )
                .offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }
                .transformable(state = state)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offset += dragAmount
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { /* Called when the gesture starts */ },
                        onDoubleTap = { scale = 1f },
                        onLongPress = { /* Called on Long Press */ },
                        onTap = { /* Called on Tap */ }
                    )
                }
        )

        Surface(
            elevation = 4.dp,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column() {
                Icon(
                    painterResource(id = if (openMenu.value) R.drawable.ic_down else R.drawable.ic_up),
                    contentDescription = stringResource(id = R.string.open),
                    modifier = Modifier
                        .clickable {
                            openMenu.value = openMenu.value.not()
                        }
                        .align(Alignment.CenterHorizontally)
                )

                if (openMenu.value) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Alpha: ")

                        Slider(
                            alphaSliderPosition,
                            onValueChange = {
                                alphaSliderPosition = it
                                colorFilterState.value =
                                    ColorFilter.colorMatrix(ColorMatrix(
                                        floatArrayOf(
                                            1f, 0f, 0f, it, 000f,
                                            0f, 1f, 0f, it, 000f,
                                            0f, 0f, 1f, it, 000f,
                                            0f, 0f, 0f, 1f, 000f
                                        )
                                    ))
                            },
                            valueRange = -0.75f .. 0.75f,
                            steps = 100
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Contrast: ")

                        Slider(
                            contrastSliderPosition,
                            onValueChange = {
                                contrastSliderPosition = it
                                colorFilterState.value =
                                    makeColorFilter(contrastSliderPosition, alphaSliderPosition)
                            },
                            valueRange = 0f .. 1.75f,
                            steps = 256
                        )
                    }

                    Row(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_rotate_left),
                            contentDescription = stringResource(id = R.string.content_description_rotate_left),
                            tint = MaterialTheme.colors.onSurface,
                            modifier = Modifier.clickable {
                                coroutineScope.launch {
                                    state.animateRotateBy(-90f)
                                }
                            }
                        )
                        Icon(
                            painterResource(id = R.drawable.ic_rotate_right),
                            contentDescription = stringResource(id = R.string.content_description_rotate_right),
                            tint = MaterialTheme.colors.onSurface,
                            modifier = Modifier
                                .clickable {
                                    coroutineScope.launch {
                                        state.animateRotateBy(90f)
                                    }
                                }
                                .padding(start = 8.dp)
                        )
                        Icon(
                            painterResource(id = R.drawable.ic_flip),
                            contentDescription = stringResource(id = R.string.content_description_reverse_image),
                            tint = MaterialTheme.colors.onSurface,
                            modifier = Modifier
                                .clickable {
                                    coroutineScope.launch {
                                        rotationY = if (rotationY == 0f) 180f else 0f
                                    }
                                }
                                .padding(start = 8.dp)
                        )

                        Icon(
                            painterResource(id = R.drawable.ic_brush),
                            contentDescription = stringResource(id = R.string.content_description_reverse_color),
                            tint = Color(0xCCCDDC39),
                            modifier = Modifier
                                .clickable {
                                    colorFilterState.value =
                                        ColorFilter.colorMatrix(
                                            ColorMatrix(
                                                floatArrayOf(
                                                    -1f, 0f, 0f, 0f, 255f,
                                                    0f, -1f, 0f, 0f, 255f,
                                                    0f, 0f, -1f, 0f, 255f,
                                                    0f, 0f, 0f, 1f, 255f
                                                )
                                            )
                                        )
                                }
                                .padding(start = 8.dp)
                        )

                        Icon(
                            painterResource(id = R.drawable.ic_brush),
                            contentDescription = stringResource(id = R.string.content_description_sepia_color_filter),
                            tint = Color(0xDDFF5722),
                            modifier = Modifier
                                .clickable {
                                    colorFilterState.value =
                                        ColorFilter.colorMatrix(
                                            ColorMatrix(
                                                floatArrayOf(
                                                    0.9f, 0f, 0f, 0f, 000f,
                                                    0f, 0.7f, 0f, 0f, 000f,
                                                    0f, 0f, 0.4f, 0f, 000f,
                                                    0f, 0f, 0f, 1f, 000f
                                                )
                                            )
                                        )
                                }
                                .padding(start = 8.dp)
                        )

                        Icon(
                            painterResource(id = R.drawable.ic_brush),
                            contentDescription = stringResource(id = R.string.content_description_gray_scale),
                            tint = Color(0xFFAAAAAA),
                            modifier = Modifier
                                .clickable {
                                    colorFilterState.value =
                                        ColorFilter.colorMatrix(ColorMatrix().also {
                                            it.setToSaturation(
                                                0.0f
                                            )
                                        })
                                }
                                .padding(start = 8.dp)
                        )

                    }
                }
            }
        }
    }

    (LocalContext.current as? ViewModelStoreOwner)?.let {
        ViewModelProvider(it).get(ContentViewModel::class.java).hideAppBar()
    }
}

private fun makeColorFilter(
    contrastSliderPosition: Float,
    alphaSliderPosition: Float
): ColorFilter {
    val v = max(contrastSliderPosition, 0f) + 1f
    val o = -128 * (v - 1)
    return ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                v, 0f, 0f, alphaSliderPosition, o,
                0f, v, 0f, alphaSliderPosition, o,
                0f, 0f, v, alphaSliderPosition, o,
                0f, 0f, 0f, 1f, 000f
            )
        )
    )
}
