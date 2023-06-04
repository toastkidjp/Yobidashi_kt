/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.floating.view

import android.net.Uri
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.floating.FloatingPreviewViewModel
import jp.toastkid.yobidashi.browser.floating.WebViewInitializer
import jp.toastkid.yobidashi.browser.view.BrowserTitle
import jp.toastkid.yobidashi.browser.webview.DarkModeApplier
import jp.toastkid.yobidashi.browser.webview.factory.WebViewFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun FloatingPreviewUi(uri: Uri) {
    val context = LocalContext.current as? ComponentActivity ?: return
    val preferenceApplier = PreferenceApplier(context)

    val viewModel = remember { FloatingPreviewViewModel() }
    val contentViewModel = viewModel(ContentViewModel::class.java, context)
    val browserViewModel = viewModel(BrowserViewModel::class.java, context)

    val coroutineScope = rememberCoroutineScope()

    val webView = remember {
        val view = WebViewFactory().make(context)
        WebViewInitializer.launch(view, viewModel)
        DarkModeApplier().invoke(view, preferenceApplier.useDarkMode())
        view
    }

    Column(modifier = Modifier.height(400.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
                .padding(8.dp)
                .clickable {
                    val currentUri = viewModel.url.value?.toUri() ?: return@clickable
                    browserViewModel.open(currentUri)
                }
        ) {
            AsyncImage(
                model = viewModel.icon.value,
                contentDescription = stringResource(id = R.string.image),
                modifier = Modifier
                    .size(36.dp)
                    .padding(end = 8.dp)
            )

            BrowserTitle(
                viewModel.progress,
                viewModel.title,
                viewModel.url,
                Modifier.weight(1f)
            )

            Icon(
                painterResource(id = R.drawable.ic_close),
                stringResource(id = R.string.close),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.clickable {
                    close(webView, coroutineScope, contentViewModel)
                }
            )
        }

        val progress = viewModel.progress.value?.toFloat() ?: 100f
        if (progress < 75) {
            LinearProgressIndicator(
                progress = progress / 100f,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.height(1.dp)
            )
        }

        AndroidView(
            factory = {
                webView
            },
            modifier = Modifier.verticalScroll(rememberScrollState())
        )
    }

    LaunchedEffect(webView.hashCode(), block = {
        webView.loadUrl(uri.toString())
    })

    DisposableEffect("dispose") {
        onDispose {
            webView.destroy()
        }
    }

    BackHandler {
        if (webView.canGoBack()) {
            webView.goBack()
            return@BackHandler
        }

        close(webView, coroutineScope, contentViewModel)
    }
}

private fun close(
    webView: WebView,
    coroutineScope: CoroutineScope,
    contentViewModel: ContentViewModel
) {
    webView.destroy()
    coroutineScope.launch {
        contentViewModel.hideBottomSheet()
    }
}