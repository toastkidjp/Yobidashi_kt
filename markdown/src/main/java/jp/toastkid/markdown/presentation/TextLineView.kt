/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.markdown.presentation

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.markdown.domain.service.LinkBehaviorService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference

@Composable
fun TextLineView(text: String, textStyle: TextStyle, onSelected: (String) -> Unit = {}, modifier: Modifier) {
    val context = LocalContext.current as? ViewModelStoreOwner ?: return

    val viewModel = remember {
        val linkBehaviorService = LinkBehaviorService(
            ViewModelProvider(context).get(ContentViewModel::class.java),
            { true }
        )
        TextLineViewModel(linkBehaviorService)
    }

    val layoutResult = AtomicReference<TextLayoutResult?>(null)

    BasicText(
        viewModel.annotatedString(),
        style = textStyle,
        onTextLayout = {
            viewModel.putLayoutResult(it)
            layoutResult.set(it)
        },
        modifier = modifier.pointerInput(viewModel.annotatedString()) {
            detectTapGestures(
                onTap = { offset ->
                    val textLayoutResult = layoutResult.get() ?: return@detectTapGestures
                    val offsetForPosition = textLayoutResult.getOffsetForPosition(offset)
                    viewModel.onClick(offsetForPosition)
                },
                onLongPress = { offset ->
                    val textLayoutResult = layoutResult.get() ?: return@detectTapGestures
                    val offsetForPosition = textLayoutResult.getOffsetForPosition(offset)
                    viewModel.onLongClick(offsetForPosition)
                }
            )
        }
    )

    LaunchedEffect(text) {
        withContext(Dispatchers.IO) {
            viewModel.launch(text)
        }
    }
}