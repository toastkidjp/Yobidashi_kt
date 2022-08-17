/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.image.preview

import android.graphics.BitmapFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateRotateBy
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ResistanceConfig
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.SwipeableState
import androidx.compose.material.Text
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
import jp.toastkid.image.Image
import jp.toastkid.image.R
import jp.toastkid.image.factory.GifImageLoaderFactory
import jp.toastkid.image.preview.attach.AttachToAnyAppUseCase
import jp.toastkid.image.preview.attach.AttachToThisAppBackgroundUseCase
import jp.toastkid.image.preview.detail.ExifInformationExtractorUseCase
import jp.toastkid.image.preview.viewmodel.ImagePreviewViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.ui.dialog.ConfirmDialog
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun ImagePreviewUi(images: List<Image>, initialIndex: Int) {
    val imageLoader = GifImageLoaderFactory().invoke(LocalContext.current)
    val coroutineScope = rememberCoroutineScope()

    val viewModel = remember { ImagePreviewViewModel() }
    LaunchedEffect(key1 = Unit, block = {
        viewModel.setIndex(initialIndex)
        viewModel.replaceImages(images)
    })

    val contentViewModel = (LocalContext.current as? ViewModelStoreOwner)?.let {
        ViewModelProvider(it).get(ContentViewModel::class.java)
    }
    val context = LocalContext.current ?: return

    val sizePx = with(LocalDensity.current) { 100.dp.toPx() }
    val anchors = mapOf(sizePx to -1, 0f to 0, -sizePx to 1)
    val swipeableState = SwipeableState(
        initialValue = 0,
        confirmStateChange = {
            if (it == -1) {
                viewModel.moveToPrevious()
            } else if (it == 1) {
                viewModel.moveToNext()
            }
            true
        }
    )

    Box {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(viewModel.getCurrentImage().path).crossfade(true).build(),
            imageLoader = imageLoader,
            contentDescription = viewModel.getCurrentImage().name,
            colorFilter = viewModel.colorFilterState.value,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = viewModel.scale.value,
                    scaleY = viewModel.scale.value,
                    rotationY = viewModel.rotationY.value,
                    rotationZ = viewModel.rotationZ.value
                )
                .offset {
                    IntOffset(
                        viewModel.offset.value.x.toInt(),
                        viewModel.offset.value.y.toInt()
                    )
                }
                .transformable(state = viewModel.state)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        viewModel.offset.value += dragAmount
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { /* Called when the gesture starts */ },
                        onDoubleTap = { viewModel.scale.value = 1f },
                        onLongPress = { /* Called on Long Press */ },
                        onTap = { /* Called on Tap */ }
                    )
                }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.Center)
                .offset { IntOffset(swipeableState.offset.value.roundToInt(), 0) }
                .swipeable(
                    swipeableState,
                    anchors = anchors,
                    thresholds = { _, _ -> FractionalThreshold(0.75f) },
                    resistance = ResistanceConfig(0.5f),
                    orientation = Orientation.Horizontal
                )
        ) {
        }

        Surface(
            elevation = 4.dp,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column {
                Icon(
                    painterResource(id = if (viewModel.openMenu.value) R.drawable.ic_down else R.drawable.ic_up),
                    contentDescription = stringResource(id = R.string.open),
                    modifier = Modifier
                        .clickable {
                            viewModel.openMenu.value = viewModel.openMenu.value.not()
                        }
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally)
                )

                if (viewModel.openMenu.value) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(stringResource(R.string.title_alpha_slider))

                        Slider(
                            viewModel.alphaSliderPosition.value,
                            onValueChange = {
                                viewModel.alphaSliderPosition.value = it
                                viewModel.updateColorFilter()
                            },
                            valueRange = -0.75f .. 0.75f,
                            steps = 100
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(stringResource(R.string.title_contrast_slider))

                        Slider(
                            viewModel.contrastSliderPosition.value,
                            onValueChange = {
                                viewModel.contrastSliderPosition.value = it
                                viewModel.updateColorFilter()
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
                                    viewModel.state.animateRotateBy(-90f)
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
                                        viewModel.state.animateRotateBy(90f)
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
                                        viewModel.rotationY.value =
                                            if (viewModel.rotationY.value == 0f) 180f else 0f
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
                                    viewModel.reverse.value = viewModel.reverse.value.not()
                                    viewModel.updateColorFilter()
                                }
                                .padding(start = 8.dp)
                        )

                        Icon(
                            painterResource(id = R.drawable.ic_brush),
                            contentDescription = stringResource(id = R.string.content_description_sepia_color_filter),
                            tint = Color(0xDDFF5722),
                            modifier = Modifier
                                .clickable {
                                    viewModel.colorFilterState.value =
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
                                    viewModel.saturation.value = viewModel.saturation.value.not()
                                    viewModel.updateColorFilter()
                                }
                                .padding(start = 8.dp)
                        )

                        Box(
                            Modifier
                                .clickable { viewModel.openOtherMenu.value = true }
                                .padding(start = 8.dp)
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_set_to),
                                contentDescription = stringResource(id = R.string.content_description_set_to),
                                tint = MaterialTheme.colors.onSurface
                            )

                            DropdownMenu(
                                viewModel.openOtherMenu.value,
                                onDismissRequest = { viewModel.openOtherMenu.value = false }
                            ) {
                                DropdownMenuItem(
                                    onClick = {
                                        viewModel.openOtherMenu.value = false
                                        contentViewModel ?: return@DropdownMenuItem
                                        val image = viewModel.getCurrentImage()
                                        AttachToThisAppBackgroundUseCase(contentViewModel)
                                            .invoke(context, image.path.toUri(), BitmapFactory.decodeFile(image.path))
                                    }
                                ) {
                                    Text(
                                        stringResource(id = R.string.this_app),
                                        fontSize = 20.sp
                                    )
                                }
                                DropdownMenuItem(
                                    onClick = {
                                        viewModel.openOtherMenu.value = false
                                        contentViewModel ?: return@DropdownMenuItem
                                        AttachToAnyAppUseCase({ context.startActivity(it) })
                                            .invoke(context, BitmapFactory.decodeFile(viewModel.getCurrentImage().path))
                                    }
                                ) {
                                    Text(
                                        stringResource(id = R.string.other_app),
                                        fontSize = 20.sp
                                    )
                                }
                            }
                        }

                        Icon(
                            painterResource(id = R.drawable.ic_info_white),
                            contentDescription = "Information",
                            tint = MaterialTheme.colors.onSurface,
                            modifier = Modifier
                                .clickable {
                                    viewModel.openDialog.value = true
                                }
                                .padding(start = 8.dp)
                        )
                    }

                }
            }
        }
    }

    if (viewModel.openDialog.value) {
        val inputStream = FileInputStream(File(viewModel.getCurrentImage().path))
        val exifInterface = ExifInterface(inputStream)
        ConfirmDialog(
            visibleState = viewModel.openDialog,
            title = viewModel.getCurrentImage().name,
            message = ExifInformationExtractorUseCase().invoke(exifInterface) ?: "Not found"
        )
    }

    contentViewModel?.hideAppBar()
}

