/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.webview.usecase

import android.net.Uri
import android.webkit.WebView

class RedirectionUseCase {

    operator fun invoke(view: WebView?, uri: Uri) {
        val key = hostAndKeys.get(uri.host) ?: return
        val redirectTo = Uri.decode(uri.getQueryParameter(key))
        view?.loadUrl(redirectTo)
    }

    companion object {

        private val hostAndKeys = mapOf(
            "app.adjust.com" to "redirect",
            "approach.yahoo.co.jp" to "src"
        )

        fun isTarget(uri: Uri): Boolean {
            return hostAndKeys.containsKey(uri.host)
        }

    }

}