/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.image.preview

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.image.Image
import jp.toastkid.image.R
import jp.toastkid.image.preview.attach.AttachToAnyAppUseCase
import jp.toastkid.image.preview.attach.AttachToThisAppBackgroundUseCase
import jp.toastkid.image.preview.detail.ExifInformationExtractorUseCase
import jp.toastkid.image.preview.viewmodel.ImagePreviewViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.ui.dialog.ConfirmDialog
import jp.toastkid.ui.image.EfficientImage
import jp.toastkid.ui.modifier.onTransform
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun ImagePreviewUi(
    images: List<Image>,
    initialIndex: Int,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val context = LocalContext.current

    val contentViewModel = remember {
        (context as? ViewModelStoreOwner)?.let {
            ViewModelProvider(it).get(ContentViewModel::class.java)
        }
    }

    val viewModel = remember { ImagePreviewViewModel(initialIndex) }
    LaunchedEffect(key1 = Unit, block = {
        viewModel.replaceImages(images)
        contentViewModel?.hideAppBar()
    })

    val coroutineScope = rememberCoroutineScope()

    Box {
        HorizontalPager(
            pageSize = PageSize.Fill,
            pageSpacing = 20.dp,
            state = viewModel.pagerState(),
            beyondViewportPageCount = 1,
            flingBehavior = PagerDefaults.flingBehavior(viewModel.pagerState(), snapPositionalThreshold = viewModel.snapPositionalThreshold()),
        ) {
            with(sharedTransitionScope) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxHeight()
                        .width(viewModel.currentSize().width.dp)
                ) {
                    EfficientImage(
                        model = viewModel.getImage(it).path,
                        contentDescription = viewModel.getImage(it).name,
                        colorFilter = viewModel.colorFilterState.value,
                        contentScale = ContentScale.FillWidth,
                        onPainter = {
                            viewModel.setPainterSize(it.intrinsicSize)
                        },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .sharedElement(
                                rememberSharedContentState(
                                    viewModel.sharedElementKey(it)
                                ),
                                animatedVisibilityScope
                            )
                            .graphicsLayer(
                                scaleX = viewModel.scale(it),
                                scaleY = viewModel.scale(it),
                                rotationY = viewModel.rotationY(it),
                                rotationZ = viewModel.rotationZ(it),
                            )
                            .onSizeChanged {
                                viewModel.setCurrentSize(it)
                            }
                            .offset {
                                viewModel.offset(it)
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = { /* Called when the gesture starts */ },
                                    onDoubleTap = {
                                        coroutineScope.launch {
                                            viewModel.zoom(it)
                                        }
                                    },
                                    onLongPress = { },
                                    onTap = { /* Called on Tap */ }
                                )
                            }
                            .pointerInput(Unit) {
                                onTransform(
                                    onGesture = { a, b, c ->
                                        coroutineScope.launch {
                                            viewModel.onGesture(a, b, c)
                                        }
                                    },
                                    onPointerInputChange = { it, panChange ->
                                        if (it.positionChanged() && viewModel.outOfRange(
                                                panChange
                                            ).not() && viewModel.currentScale() != 1f
                                        ) {
                                            it.consume()
                                            coroutineScope.launch {
                                                viewModel.resetPagerScrollState()
                                            }
                                        }
                                    }
                                )
                            }
                    )
                }
            }
        }

        Surface(
            shadowElevation = 4.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .graphicsLayer { alpha = 0.75f }
                .zIndex(0.4f)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .clickable {
                            viewModel.openMenu.value = viewModel.openMenu.value.not()
                        }
                        .fillMaxWidth()
                ) {
                    Icon(
                        painterResource(id = if (viewModel.openMenu.value) R.drawable.ic_down else R.drawable.ic_up),
                        contentDescription = stringResource(id = jp.toastkid.lib.R.string.open),
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.Center)
                    )
                }

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
                            valueRange = -5f .. 1f,
                            steps = 600,
                            colors = SliderDefaults.colors().copy(
                                activeTrackColor = MaterialTheme.colorScheme.secondary
                            )
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
                            valueRange = 0f .. 10f,
                            steps = 1000,
                            colors = SliderDefaults.colors().copy(
                                activeTrackColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.padding(16.dp)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_rotate_left),
                            contentDescription = stringResource(id = R.string.content_description_rotate_left),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.clickable {
                                coroutineScope.launch {
                                    viewModel.rotateLeft()
                                }
                            }
                        )
                        Icon(
                            painterResource(id = R.drawable.ic_rotate_right),
                            contentDescription = stringResource(id = R.string.content_description_rotate_right),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clickable {
                                    coroutineScope.launch {
                                        viewModel.rotateRight()
                                    }
                                }
                                .padding(start = 16.dp)
                        )
                        Icon(
                            painterResource(id = R.drawable.ic_flip),
                            contentDescription = stringResource(id = R.string.content_description_reverse_image),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clickable {
                                    coroutineScope.launch {
                                        viewModel.flip()
                                    }
                                }
                                .padding(start = 16.dp)
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
                                .padding(start = 16.dp)
                        )

                        Icon(
                            painterResource(id = R.drawable.ic_brush),
                            contentDescription = stringResource(id = R.string.content_description_sepia_color_filter),
                            tint = Color(0xDDFF5722),
                            modifier = Modifier
                                .clickable(onClick = viewModel::setSepia)
                                .padding(start = 16.dp)
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
                                .padding(start = 16.dp)
                        )

                        Box(
                            Modifier
                                .clickable { viewModel.openOtherMenu.value = true }
                                .padding(start = 16.dp)
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_set_to),
                                contentDescription = stringResource(id = R.string.content_description_set_to),
                                tint = MaterialTheme.colorScheme.onSurface
                            )

                            DropdownMenu(
                                viewModel.openOtherMenu.value,
                                onDismissRequest = viewModel::closeOtherMenu
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            stringResource(id = R.string.this_app),
                                            fontSize = 20.sp
                                        )
                                    },
                                    onClick = {
                                        viewModel.closeOtherMenu()
                                        contentViewModel ?: return@DropdownMenuItem
                                        val image = viewModel.getCurrentImage()
                                        AttachToThisAppBackgroundUseCase(contentViewModel)
                                            .invoke(context, image.path.toUri(), BitmapFactory.decodeFile(image.path))
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            stringResource(id = R.string.other_app),
                                            fontSize = 20.sp
                                        )
                                    },
                                    onClick = {
                                        viewModel.closeOtherMenu()
                                        contentViewModel ?: return@DropdownMenuItem
                                        AttachToAnyAppUseCase(context::startActivity)
                                            .invoke(context, BitmapFactory.decodeFile(viewModel.getCurrentImage().path))
                                    }
                                )
                            }
                        }


                        Icon(
                            painterResource(id = jp.toastkid.lib.R.drawable.ic_remove_circle),
                            contentDescription = stringResource(jp.toastkid.lib.R.string.reset),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clickable {
                                    viewModel.resetImageCondition()
                                }
                                .padding(start = 16.dp)
                        )

                        Icon(
                            painterResource(id = R.drawable.ic_info_white),
                            contentDescription = stringResource(R.string.content_description_information),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clickable {
                                    viewModel.openDialog.value = true
                                }
                                .padding(start = 16.dp)
                        )
                    }

                }
            }
        }
    }

    if (viewModel.openDialog.value) {
        val message = BufferedInputStream(FileInputStream(File(viewModel.getCurrentImage().path))).use {
            val exifInterface = ExifInterface(it)
            ExifInformationExtractorUseCase().invoke(exifInterface)
        }

        ConfirmDialog(
            title = viewModel.getCurrentImage().name,
            message = message,
            onDismissRequest = viewModel::closeDialog
        )
    }

    LaunchedEffect(viewModel.pagerState().settledPage) {
        coroutineScope.launch {
            viewModel.clearPreviousState()
        }
    }

    BackHandler(viewModel.openMenu.value) {
        viewModel.openMenu.value = false
    }
}
