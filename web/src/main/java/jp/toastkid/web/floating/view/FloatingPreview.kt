/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.web.floating.view

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.ui.image.EfficientImage
import jp.toastkid.web.floating.FloatingPreviewViewModel
import jp.toastkid.web.floating.WebViewInitializer
import jp.toastkid.web.view.TitleUrlBox
import jp.toastkid.web.webview.DarkModeApplier
import jp.toastkid.web.webview.factory.WebViewFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingPreviewUi(uri: Uri) {
    val context = LocalContext.current as? ComponentActivity ?: return

    val viewModel = remember { FloatingPreviewViewModel() }
    val contentViewModel = viewModel(ContentViewModel::class.java, context)

    val coroutineScope = rememberCoroutineScope()

    val webView = remember {
        val view = WebViewFactory().make(context)
        WebViewInitializer.launch(view, viewModel)
        DarkModeApplier().invoke(view, PreferenceApplier(context).useDarkMode())
        view
    }

    val sheetState = rememberModalBottomSheetState()

    val onClose = remember {
        {
            webView.destroy()
            coroutineScope.launch {
                sheetState.hide()
                contentViewModel.switchFloatingPreviewUi()
            }
            Unit
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onClose,
        tonalElevation = 1.dp,
        containerColor = MaterialTheme.colorScheme.primary,
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .drawBehind { drawRect(primaryColor) }
                .height(52.dp)
                .padding(8.dp)
                .clickable {
                    val currentUri = viewModel.url().toUri()
                    contentViewModel.open(currentUri)
                }
        ) {
            EfficientImage(
                model = viewModel.icon(),
                contentDescription = stringResource(id = jp.toastkid.lib.R.string.image),
                modifier = Modifier
                    .size(36.dp)
                    .padding(end = 8.dp)
            )

            TitleUrlBox(
                viewModel.title(),
                viewModel.url(),
                viewModel.progress(),
                Modifier.weight(1f)
            )

            Icon(
                painterResource(id = jp.toastkid.lib.R.drawable.ic_close),
                stringResource(id = jp.toastkid.lib.R.string.close),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.clickable(onClick = onClose)
            )
        }

        val progress = viewModel.progress().toFloat()
        if (progress < 75) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.height(1.dp)
            )
        }

        AndroidView(
            factory = {
                webView
            }
        )

        BackHandler {
            if (webView.canGoBack()) {
                webView.goBack()
                return@BackHandler
            }

            onClose()
        }
    }

    LaunchedEffect(webView.hashCode(), block = {
        webView.loadUrl(uri.toString())
    })

    DisposableEffect("dispose") {
        onDispose {
            webView.destroy()
        }
    }
}
