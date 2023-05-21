/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.webview.factory

import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.browser.block.AdRemover

class OnBackgroundWebViewClientFactory(
    private val preferenceApplier: PreferenceApplier,
    private val adRemover: AdRemover
) {

    /**
     * Add onPageFinished and onPageStarted.
     */
    operator fun invoke(): WebViewClient = object : WebViewClient() {

        override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest
        ): WebResourceResponse? =
            if (preferenceApplier.adRemove) {
                adRemover(request.url.toString())
            } else {
                super.shouldInterceptRequest(view, request)
            }
    }

    companion object {

        fun with(context: Context) = OnBackgroundWebViewClientFactory(
            PreferenceApplier(context),
            AdRemover.make(context.assets)
        )

    }

}