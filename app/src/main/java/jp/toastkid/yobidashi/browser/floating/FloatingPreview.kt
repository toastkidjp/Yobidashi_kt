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
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.widget.PopupWindow
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.get
import androidx.core.view.isEmpty
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserViewModel
import jp.toastkid.yobidashi.databinding.PopupFloatingPreviewBinding

/**
 * Floating preview.
 *
 * @author toastkidjp
 */
class FloatingPreview(context: Context) {

    private val popupWindow = PopupWindow(context)

    private val binding: PopupFloatingPreviewBinding

    private var viewModel: FloatingPreviewViewModel? = null

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = DataBindingUtil.inflate(layoutInflater, LAYOUT_ID, null, false)
        binding.preview = this

        popupWindow.contentView = binding.root

        (context as? FragmentActivity)?.also {
            viewModel = ViewModelProviders.of(context).get(FloatingPreviewViewModel::class.java)

            viewModel?.title?.observe(context, Observer {
                binding.title.text = it
            })

            viewModel?.icon?.observe(context, Observer {
                binding.icon.setImageBitmap(it)
            })

            viewModel?.url?.observe(context, Observer {
                binding.url.text = it
            })
        }

        popupWindow.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context, R.color.transparent)))

        popupWindow.setOnDismissListener {
            extractWebView()?.also {
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

        setSlidingListener()

        binding.icon.setImageBitmap(null)

        webView.isEnabled = true
        webView.onResume()

        WebViewInitializer()(webView, url)

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

    fun openCurrentWithNewTab() {
        extractWebView()?.also {
            openNewTabWithUrl(it.url)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setSlidingListener() {
        binding.header.setOnTouchListener(SlidingTouchListener(binding.contentPanel))
    }

    /**
     * Open passed url by new tab.
     *
     * @param url URL string
     */
    private fun openNewTabWithUrl(url: String) {
        (binding.root.context as? FragmentActivity)?.also { fragmentActivity ->
            ViewModelProviders.of(fragmentActivity)
                    .get(BrowserViewModel::class.java)
                    .open(url.toUri())
        }

        hide()
    }

    private fun extractWebView(): WebView? {
        if (binding.previewContainer.isEmpty()) {
            return null
        }
        val view = binding.previewContainer.get(0)
        return view as? WebView
    }

    fun isVisible() = popupWindow.isShowing

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.popup_floating_preview

        private const val DURATION_MS = 200L

        private const val SPECIAL_WEB_VIEW_ID = "preview"

        fun getSpecialId() = SPECIAL_WEB_VIEW_ID
    }
}