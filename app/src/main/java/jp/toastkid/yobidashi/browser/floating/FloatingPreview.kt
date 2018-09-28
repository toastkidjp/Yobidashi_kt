/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.floating

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.core.net.toUri
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.main.MainActivity

/**
 * @author toastkidjp
 */
class FloatingPreview(private val parent: ViewGroup) {

    operator fun invoke(webView: WebView, url: String) {
        if (parent.childCount != 0) {
            parent.removeAllViews()
        }
        parent.visibility = View.VISIBLE

        setLayoutParams(webView)

        webView.isEnabled = true
        webView.onResume()

        setWebViewClient(webView, url)

        parent.addView(webView)

        webView.loadUrl(url)
    }

    private fun setLayoutParams(webView: WebView) {
        val layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )
        layoutParams.height =
                parent.context.resources.getDimensionPixelSize(R.dimen.floating_preview_height)
        layoutParams.gravity = Gravity.BOTTOM
        webView.layoutParams = layoutParams
    }

    private fun setWebViewClient(webView: WebView, url: String) {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                parent.context?.let {
                    it.startActivity(MainActivity.makeBrowserIntent(it, url.toUri()))
                }
                parent.visibility = View.GONE
                parent.removeAllViews()
                return false
            }
        }
    }

    fun hide(webView: WebView?) {
        parent.visibility = View.GONE
        webView?.isEnabled = false
        webView?.onPause()
    }
}