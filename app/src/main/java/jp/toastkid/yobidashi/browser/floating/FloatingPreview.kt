/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.floating

import android.annotation.SuppressLint
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
 * Floating preview.
 *
 * @author toastkidjp
 */
class FloatingPreview(private val binding: ContentFloatingPreviewBinding) {

    /**
     * Invoke floating preview.
     *
     * @param webView [WebView]
     * @param url URL string
     */
    operator fun invoke(webView: WebView, url: String) {
        if (binding.previewContainer.childCount != 0) {
            binding.previewContainer.removeAllViews()
        }
        binding.previewBackground.visibility = View.VISIBLE

        binding.previewBackground.setOnClickListener { hide(webView) }

        binding.header.setOnClickListener { openNewTabWithUrl(url) }

        setSlidingListener()

        binding.icon.setImageBitmap(null)

        webView.isEnabled = true
        webView.onResume()

        initializeWebView(webView, url)

        binding.previewContainer.addView(webView)

        webView.loadUrl(url)

        binding.contentPanel.animate()
                .y(binding.contentPanel.context.resources.displayMetrics.heightPixels * 0.6f)
                .setDuration(0)
                .start()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setSlidingListener() {
        binding.header.setOnTouchListener(SlidingTouchListener(binding.contentPanel))
    }

    /**
     * Initialize WebView.
     *
     * @param webView [WebView]
     * @param url URL string
     */
    private fun initializeWebView(webView: WebView, url: String) {
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

    /**
     * Open passed url by new tab.
     *
     * @param url URL string
     */
    private fun openNewTabWithUrl(url: String) {
        binding.root.context?.let {
            it.startActivity(MainActivity.makeBrowserIntent(it, url.toUri()))
            binding.previewBackground.visibility = View.GONE
            binding.previewContainer.removeAllViews()
        }
    }

    /**
     * Hide this preview.
     *
     * @param webView [WebView]
     */
    fun hide(webView: WebView?) {
        binding.previewBackground.visibility = View.GONE
        webView?.isEnabled = false
        webView?.onPause()
    }
}