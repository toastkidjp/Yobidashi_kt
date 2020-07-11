/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.download.image

import android.webkit.WebView
import jp.toastkid.lib.Urls
import jp.toastkid.yobidashi.libs.network.DownloadAction

/**
 * @author toastkidjp
 */
class AllImageDownloaderService {

    operator fun invoke(webView: WebView?) {
        webView?.evaluateJavascript(SCRIPT) { result ->
            if (result.isNullOrBlank() || !result.contains(DELIMITER)) {
                return@evaluateJavascript
            }

            DownloadAction(webView.context)
                    .invoke(result.split(DELIMITER).filter { Urls.isValidUrl(it) })
        }
    }

    companion object {
        private const val DELIMITER = ","

        private val SCRIPT =
                """
                var images = document.getElementsByTagName('img');
                var content = "";
                for (var i = 0; i < images.length; i++) {
                    content = content + images[i].src + ',';
                }
                content;
                """.trimIndent()
    }

}