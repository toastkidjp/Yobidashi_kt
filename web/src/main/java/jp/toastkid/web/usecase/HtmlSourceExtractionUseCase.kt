/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.web.usecase

import android.os.Build
import android.webkit.ValueCallback
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.annotation.UiThread

class HtmlSourceExtractionUseCase {

    /**
     * WebView.evaluateJavascript should be called from UI thread.
     *
     * @param webView [WebView]
     * @param callback [ValueCallback<String>]
     */
    @UiThread
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    operator fun invoke(webView: WebView?, callback: ValueCallback<String>) {
        if (webView?.url?.startsWith(EXCEPTING_URL) == true) {
            return
        }

        webView?.evaluateJavascript(SCRIPT, callback)
    }

    companion object {

        private const val SCRIPT = "document.documentElement.outerHTML;"

        private const val EXCEPTING_URL = "https://accounts.google.com/"

    }
}