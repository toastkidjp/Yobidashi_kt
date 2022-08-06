/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.image.preview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateRotateBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import jp.toastkid.image.Image
import jp.toastkid.image.R
import jp.toastkid.image.factory.GifImageLoaderFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ImagePreviewUi(images: List<Image>, initialIndex: Int) {
    val imageLoader = GifImageLoaderFactory().invoke(LocalContext.current)

    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        scale *= zoomChange
        rotation += rotationChange
        offset += offsetChange
    }

    val coroutineScope = rememberCoroutineScope()

    val index = remember { mutableStateOf(initialIndex) }

    val image = images[index.value]

    Box {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image.path).crossfade(true).build(),
            imageLoader = imageLoader,
            contentDescription = image.name,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    rotationZ = rotation,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .transformable(state = state)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { /* Called when the gesture starts */ },
                        onDoubleTap = { scale = 1f },
                        onLongPress = { /* Called on Long Press */ },
                        onTap = { /* Called on Tap */ }
                    )
                }
        )
        Row(
           modifier = Modifier.align(Alignment.BottomCenter)
               .background(MaterialTheme.colors.surface)
        ) {
            Icon(
                painterResource(id = R.drawable.ic_rotate_left),
                contentDescription = stringResource(id = R.string.content_description_reverse_image),
                tint = MaterialTheme.colors.onSurface,
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        state.animateRotateBy(-90f)
                    }
                }
            )
            Icon(
                painterResource(id = R.drawable.ic_rotate_right),
                contentDescription = stringResource(id = R.string.content_description_reverse_image),
                tint = MaterialTheme.colors.onSurface,
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        state.animateRotateBy(90f)
                    }
                }
                    .padding(start = 8.dp)
            )
        }
    }
}
