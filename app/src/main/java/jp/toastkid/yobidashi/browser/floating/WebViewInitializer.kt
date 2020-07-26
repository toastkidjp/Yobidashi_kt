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
import android.os.Build
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.yobidashi.browser.block.AdRemover
import jp.toastkid.lib.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
class WebViewInitializer {

    /**
     * Initialize WebView.
     *
     * @param webView [WebView]
     * @param url URL string
     */
    operator fun invoke(webView: WebView) {
        val context = webView.context as? FragmentActivity ?: return

        val preferenceApplier = PreferenceApplier(context)

        val adRemover = AdRemover.make(context.assets)

        val viewModel = ViewModelProvider(context).get(FloatingPreviewViewModel::class.java)

        webView.webViewClient = object : WebViewClient() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
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

            override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                super.onReceivedIcon(view, icon)
                viewModel.newIcon(icon)
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)

                viewModel.newProgress(newProgress)
            }
        }
    }
}