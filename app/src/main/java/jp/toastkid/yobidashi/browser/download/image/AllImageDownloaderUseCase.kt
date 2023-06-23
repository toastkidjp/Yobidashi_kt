/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.download.image

import android.webkit.WebView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.network.DownloadAction
import jp.toastkid.yobidashi.libs.network.NetworkChecker

/**
 * @author toastkidjp
 */
class AllImageDownloaderUseCase(private val downloadAction: DownloadAction) {

    operator fun invoke(webView: WebView?) {
        webView?.evaluateJavascript(SCRIPT) { result ->
            if (result.isNullOrBlank() || !result.contains(DELIMITER)) {
                return@evaluateJavascript
            }

            if (PreferenceApplier(webView.context).wifiOnly && NetworkChecker().isUnavailableWiFi(webView.context)) {
                (webView.context as? ViewModelStoreOwner)?.let {
                    ViewModelProvider(it).get(ContentViewModel::class.java)
                        .snackShort(R.string.message_wifi_not_connecting)
                }
                return@evaluateJavascript
            }

            downloadAction.invoke(result.split(DELIMITER).filter { Urls.isValidUrl(it) })
        }
    }

    companion object {
        private const val DELIMITER = ","

        private val SCRIPT =
                """
                var images = document.getElementsByTagName('img');
                var content = "";
                for (var i = 0; i < images.length; i++) {
                    content = content + images[i].src + '$DELIMITER';
                }
                content;
                """.trimIndent()
    }

}