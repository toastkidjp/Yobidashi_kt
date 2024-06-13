/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.markdown.presentation

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.markdown.domain.service.LinkBehaviorService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TextLineView(text: String, textStyle: TextStyle, modifier: Modifier) {
    val context = LocalContext.current as? ViewModelStoreOwner ?: return

    val viewModel = remember {
        val linkBehaviorService = LinkBehaviorService(
            ViewModelProvider(context).get(ContentViewModel::class.java),
            { true }
        )
        TextLineViewModel(linkBehaviorService)
    }

    ClickableText(
        viewModel.annotatedString(),
        style = textStyle,
        onClick = viewModel::onClick,
        onTextLayout = viewModel::putLayoutResult,
        modifier = modifier
    )

    LaunchedEffect(text) {
        withContext(Dispatchers.IO) {
            viewModel.launch(text)
        }
    }
}