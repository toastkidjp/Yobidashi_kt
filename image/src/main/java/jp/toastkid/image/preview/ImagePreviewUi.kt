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
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import jp.toastkid.image.Image
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ImagePreviewUi(images: List<Image>, initialIndex: Int) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    MaterialTheme {
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(end = 16.dp),
            modifier = Modifier.fillMaxSize().background(Color.Transparent)
        ) {
            items(images) { image ->
                var scale by remember { mutableStateOf(1f) }
                var rotation by remember { mutableStateOf(0f) }
                var offset by remember { mutableStateOf(Offset.Zero) }

                val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
                    scale *= zoomChange
                    rotation += rotationChange
                    offset += offsetChange
                }

                AsyncImage(
                    model = image.path,
                    contentDescription = image.name,
                    modifier = Modifier.fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            rotationZ = rotation,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(state = state)
                )
            }
            coroutineScope.launch {
                listState.scrollToItem(initialIndex, 0)
            }
        }
    }
}
