/*
 * Copyright (c) 2020 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.web.webview.factory

import android.graphics.Bitmap
import android.os.Message
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.image.BitmapCompressor
import jp.toastkid.web.FaviconApplier
import jp.toastkid.web.view.WebTabUiViewModel
import jp.toastkid.web.webview.CustomViewSwitcher
import jp.toastkid.web.webview.GlobalWebViewPool
import java.util.concurrent.TimeUnit

class WebChromeClientFactory(
    private val browserViewModel: WebTabUiViewModel? = null,
    private val faviconApplier: FaviconApplier? = null,
    private val customViewSwitcher: CustomViewSwitcher? = null
) {

    operator fun invoke(): WebChromeClient = object : WebChromeClient() {

        private val bitmapCompressor = BitmapCompressor()

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)

            browserViewModel?.updateProgress(newProgress)
        }

        override fun onReceivedIcon(view: WebView?, favicon: Bitmap?) {
            super.onReceivedIcon(view, favicon)
            val urlStr = view?.url
            if (urlStr != null && favicon != null) {
                val file = faviconApplier?.assignFile(urlStr) ?: return
                if (System.currentTimeMillis() - file.lastModified() < TimeUnit.HOURS.toMillis(6)) {
                    return
                }
                bitmapCompressor(favicon, file)
            }
        }

        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
            super.onShowCustomView(view, callback)

            obtainContentViewModel(view)?.hideAppBar()

            customViewSwitcher?.onShowCustomView(view, callback)
        }

        override fun onHideCustomView() {
            super.onHideCustomView()

            obtainContentViewModel(GlobalWebViewPool.getLatest())?.showAppBar()

            customViewSwitcher?.onHideCustomView()
        }

        override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
        ): Boolean {
            view?.stopLoading()

            obtainContentViewModel(view)?.openNewWindow(resultMsg)

            return true
        }

        private fun obtainContentViewModel(view: View?): ContentViewModel? {
            return (view?.context as? ViewModelStoreOwner)?.let { fragmentActivity ->
                ViewModelProvider(fragmentActivity)
                    .get(ContentViewModel::class.java)
            }
        }
    }
}