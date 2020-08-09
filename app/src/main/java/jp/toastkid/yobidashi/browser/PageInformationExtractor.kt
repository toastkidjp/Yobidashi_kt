/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser

import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.core.os.bundleOf

/**
 * @author toastkidjp
 */
class PageInformationExtractor {

    operator fun invoke(webView: WebView?): Bundle {
        return webView?.let {
            val url = it.url
            if (url.isNullOrBlank()) {
                return@let bundleOf()
            }

            return@let bundleOf(
                    "url" to url,
                    "favicon" to it.favicon,
                    "title" to it.title,
                    "cookie" to CookieManager.getInstance().getCookie(url)
            )
        } ?: bundleOf()
    }
}