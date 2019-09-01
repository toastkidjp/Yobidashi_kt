/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.floating

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.get
import androidx.core.view.isEmpty
import androidx.databinding.DataBindingUtil
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.PopupFloatingPreviewBinding
import jp.toastkid.yobidashi.main.MainActivity

/**
 * Floating preview.
 *
 * @author toastkidjp
 */
class FloatingPreview(context: Context) {

    private val popupWindow = PopupWindow(context)

    private val binding: PopupFloatingPreviewBinding

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.popup_floating_preview, null, false)
        binding.preview = this

        popupWindow.contentView = binding.root

        popupWindow.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context, R.color.transparent)))

        popupWindow.setOnDismissListener {
            if (binding.previewContainer.isEmpty()) {
                return@setOnDismissListener
            }
            val view = binding.previewContainer.get(0)
            (view as? WebView)?.also {
                it.isEnabled = false
                it.onPause()
            }
        }

        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true

        popupWindow.width = WindowManager.LayoutParams.MATCH_PARENT
        popupWindow.height = WindowManager.LayoutParams.MATCH_PARENT
    }

    /**
     * Invoke floating preview.
     *
     * @param webView [WebView]
     * @param url URL string
     */
    fun show(parent: View, webView: WebView, url: String) {
        if (binding.previewContainer.childCount != 0) {
            binding.previewContainer.removeAllViews()
        }

        binding.header.setOnClickListener { openNewTabWithUrl(url) }

        setSlidingListener()

        binding.icon.setImageBitmap(null)

        webView.isEnabled = true
        webView.onResume()

        initializeWebView(webView, url)

        binding.previewContainer.addView(webView)

        webView.loadUrl(url)

        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0)

        startEnterAnimation()
    }

    private fun startEnterAnimation() {
        val heightPixels = binding.contentPanel.context.resources.displayMetrics.heightPixels

        binding.contentPanel.animate()
                .y(heightPixels.toFloat())
                .setDuration(0)
                .start()

        binding.contentPanel.animate()
                .y(heightPixels * 0.6f)
                .setDuration(DURATION_MS)
                .start()
    }

    /**
     * Hide this preview.
     */
    fun hide() {
        popupWindow.dismiss()
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

    fun isVisible() = popupWindow.isShowing

    companion object {
        private const val DURATION_MS = 200L

        private const val SPECIAL_WEB_VIEW_ID = "preview"

        fun getSpecialId() = SPECIAL_WEB_VIEW_ID
    }
}