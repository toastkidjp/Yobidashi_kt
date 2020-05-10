/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.floating

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
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
import jp.toastkid.yobidashi.browser.webview.DarkModeApplier
import jp.toastkid.yobidashi.databinding.PopupFloatingPreviewBinding
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Floating preview.
 *
 * @author toastkidjp
 */
class FloatingPreview(private val webView: WebView) {

    private val popupWindow = PopupWindow(webView.context)

    private val binding: PopupFloatingPreviewBinding

    private val darkModeApplier = DarkModeApplier()

    private var viewModel: FloatingPreviewViewModel? = null

    init {
        val context = webView.context
        val layoutInflater = LayoutInflater.from(context)
        binding = DataBindingUtil.inflate(layoutInflater, LAYOUT_ID, null, false)
        binding.preview = this
        binding.previewContainer.addView(webView)

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

        WebViewInitializer()(webView, binding.progress)
    }

    /**
     * Invoke floating preview.
     *
     * @param webView [WebView]
     * @param url URL string
     */
    fun show(parent: View, url: String) {
        setSlidingListener()

        binding.progress.progressDrawable.colorFilter =
                PorterDuffColorFilter(
                        PreferenceApplier(binding.root.context).fontColor,
                        PorterDuff.Mode.SRC_IN
                )

        binding.icon.setImageBitmap(null)

        darkModeApplier(webView, PreferenceApplier(webView.context).useDarkMode())
        webView.isEnabled = true
        webView.onResume()

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
        webView.onPause()
        webView.isEnabled = false
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

    fun dispose() {
        webView.destroy()
    }

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.popup_floating_preview

        private const val DURATION_MS = 200L

        private const val SPECIAL_WEB_VIEW_ID = "preview"
    }
}