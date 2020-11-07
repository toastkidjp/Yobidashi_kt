/*
 * Copyright (c) 2020 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.tab.background

import android.content.Context
import android.webkit.WebView
import jp.toastkid.yobidashi.browser.webview.factory.WebViewFactory
import jp.toastkid.yobidashi.browser.webview.factory.WebChromeClientFactory
import jp.toastkid.yobidashi.browser.webview.factory.WebViewClientFactory

internal class BackgroundWebViewFactoryUseCase(
        private val webViewFactory: WebViewFactory = WebViewFactory(),
        private val webViewClientFactory: WebViewClientFactory,
        private val webChromeClientFactory: WebChromeClientFactory = WebChromeClientFactory()
) {

    operator fun invoke(context: Context): WebView {
        val webView = webViewFactory.make(context)
        webView.webViewClient = webViewClientFactory.invoke()
        webView.webChromeClient = webChromeClientFactory.invoke()
        return webView
    }

}