/*
 * Copyright (c) 2020 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.webview.factory

import android.graphics.Bitmap
import android.os.Message
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.yobidashi.browser.BrowserHeaderViewModel
import jp.toastkid.yobidashi.browser.FaviconApplier
import jp.toastkid.yobidashi.browser.webview.CustomViewSwitcher
import jp.toastkid.yobidashi.libs.BitmapCompressor

class WebChromeClientFactory(
        private val browserHeaderViewModel: BrowserHeaderViewModel? = null,
        private val faviconApplier: FaviconApplier? = null,
        private val customViewSwitcher: CustomViewSwitcher? = null
) {

    operator fun invoke(): WebChromeClient = object : WebChromeClient() {

        private val bitmapCompressor = BitmapCompressor()

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)

            browserHeaderViewModel?.updateProgress(newProgress)
            browserHeaderViewModel?.stopProgress(newProgress < 65)
        }

        override fun onReceivedIcon(view: WebView?, favicon: Bitmap?) {
            super.onReceivedIcon(view, favicon)
            val urlStr = view?.url
            if (urlStr != null && favicon != null) {
                val file = faviconApplier?.assignFile(urlStr) ?: return
                bitmapCompressor(favicon, file)
            }
        }

        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
            super.onShowCustomView(view, callback)
            customViewSwitcher?.onShowCustomView(view, callback)
        }

        override fun onHideCustomView() {
            super.onHideCustomView()
            customViewSwitcher?.onHideCustomView()
        }

        override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
        ): Boolean {
            val href = view?.handler?.obtainMessage()
            view?.requestFocusNodeHref(href)
            val url = href?.data?.getString("url")?.toUri()
                    ?: return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
            view.stopLoading()

            (view.context as? FragmentActivity)?.also { fragmentActivity ->
                ViewModelProvider(fragmentActivity)
                        .get(BrowserViewModel::class.java)
                        .open(url)
            }

            return true
        }
    }
}