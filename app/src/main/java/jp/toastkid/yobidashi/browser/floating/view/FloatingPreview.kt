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
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
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
    val tint = Color(preferenceApplier.fontColor)

    val viewModel = viewModel(FloatingPreviewViewModel::class.java)
    val contentViewModel = viewModel(ContentViewModel::class.java, context)

    val coroutineScope = rememberCoroutineScope()

    val webView = remember { WebViewFactory().make(context) }
    WebViewInitializer(preferenceApplier, viewModel)(webView)
    DarkModeApplier().invoke(webView, preferenceApplier.useDarkMode())

    Column(modifier = Modifier.height(400.dp)) {
        val progressState = viewModel.progress.observeAsState()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color(preferenceApplier.color))
                .padding(8.dp)
        ) {
            AsyncImage(
                model = viewModel.icon.observeAsState().value,
                contentDescription = stringResource(id = R.string.image),
                modifier = Modifier
                    .size(36.dp)
                    .padding(end = 8.dp)
            )

            BrowserTitle(
                progressState,
                viewModel.title.observeAsState(),
                viewModel.url.observeAsState(),
                Modifier.weight(1f)
            )

            Icon(
                painterResource(id = R.drawable.ic_close),
                stringResource(id = R.string.close),
                tint = tint,
                modifier = Modifier.clickable {
                    close(webView, coroutineScope, contentViewModel)
                }
            )
        }

        val progress = progressState.value?.toFloat() ?: 100f
        if (progress < 75) {
            LinearProgressIndicator(
                progress = progress / 100f,
                color = tint,
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