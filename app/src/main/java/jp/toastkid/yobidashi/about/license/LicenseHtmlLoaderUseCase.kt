/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.about.license

import android.widget.FrameLayout
import androidx.core.view.get
import androidx.core.view.isGone
import androidx.core.view.isVisible
import jp.toastkid.yobidashi.browser.webview.factory.WebViewFactory
import java.nio.charset.StandardCharsets

/**
 * @author toastkidjp
 */
internal class LicenseHtmlLoaderUseCase(
    private val webViewFactory: WebViewFactory = WebViewFactory()
) {

    operator fun invoke(it: FrameLayout) {
        it.isVisible = !it.isVisible
        if (it.isGone || it.childCount != 0) {
            it[0].scrollTo(0, 0)
            return
        }

        val readUtf8 = LicenseContentLoaderUseCase(it.context.assets).invoke()

        val webView = webViewFactory.make(it.context)
        it.addView(webView)
        webView.loadDataWithBaseURL(
            null,
            readUtf8,
            "text/html",
            StandardCharsets.UTF_8.name(),
            null
        )
    }

}