/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.floating

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import jp.toastkid.lib.image.BitmapCompressor
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.browser.FaviconApplier
import jp.toastkid.yobidashi.browser.block.AdRemover

/**
 * @author toastkidjp
 */
class WebViewInitializer(
        private val preferenceApplier: PreferenceApplier,
        private val viewModel: FloatingPreviewViewModel
) {

    /**
     * Initialize WebView.
     *
     * @param webView [WebView]
     */
    operator fun invoke(webView: WebView) {
        val context = webView.context

        val adRemover = AdRemover.make(context.assets)

        webView.webViewClient = object : WebViewClient() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                viewModel.newIcon(null)
                viewModel.newUrl(url)
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? =
                    if (preferenceApplier.adRemove) {
                        adRemover(request.url.toString())
                    } else {
                        super.shouldInterceptRequest(view, request)
                    }

            @Suppress("OverridingDeprecatedMember")
            @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
            override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? =
                    if (preferenceApplier.adRemove) {
                        adRemover(url)
                    } else {
                        @Suppress("DEPRECATION")
                        super.shouldInterceptRequest(view, url)
                    }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                viewModel.newTitle(title)
            }

            private val faviconApplier = FaviconApplier(context)

            private val bitmapCompressor = BitmapCompressor()

            override fun onReceivedIcon(view: WebView?, favicon: Bitmap?) {
                val urlStr = view?.url
                if (viewModel.icon.value != null) {
                    return
                }

                if (urlStr != null && favicon != null) {
                    val file = faviconApplier.assignFile(urlStr) ?: return
                    if (file.exists() && file.length() != 0L) {
                        viewModel.newIcon(BitmapFactory.decodeFile(file.absolutePath))
                        return
                    }

                    bitmapCompressor.invoke(favicon, file)
                    viewModel.newIcon(favicon)
                }
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)

                viewModel.newProgress(newProgress)
            }
        }
    }
}