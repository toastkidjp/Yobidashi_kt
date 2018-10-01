/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.floating

import android.graphics.Bitmap
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri
import jp.toastkid.yobidashi.databinding.ContentFloatingPreviewBinding
import jp.toastkid.yobidashi.main.MainActivity

/**
 * @author toastkidjp
 */
class FloatingPreview(private val binding: ContentFloatingPreviewBinding) {

    operator fun invoke(webView: WebView, url: String) {
        if (binding.previewContainer.childCount != 0) {
            binding.previewContainer.removeAllViews()
        }
        binding.previewBackground.visibility = View.VISIBLE

        binding.previewBackground.setOnClickListener { hide(webView) }

        binding.header.setOnClickListener { openNewTabWithUrl(url) }

        binding.icon.setImageBitmap(null)

        webView.isEnabled = true
        webView.onResume()

        setWebViewClient(webView, url)

        binding.previewContainer.addView(webView)

        webView.loadUrl(url)
    }

    private fun setWebViewClient(webView: WebView, url: String) {
        binding.url.text = url

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                openNewTabWithUrl(url)
                return false
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                title?.let { binding.title.text = it }
            }

            override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                super.onReceivedIcon(view, icon)
                icon?.let { binding.icon.setImageBitmap(icon) }
            }
        }
    }

    private fun openNewTabWithUrl(url: String) {
        binding.root.context?.let {
            it.startActivity(MainActivity.makeBrowserIntent(it, url.toUri()))
            binding.previewBackground.visibility = View.GONE
            binding.previewContainer.removeAllViews()
        }
    }

    fun hide(webView: WebView?) {
        binding.previewBackground.visibility = View.GONE
        webView?.isEnabled = false
        webView?.onPause()
    }
}