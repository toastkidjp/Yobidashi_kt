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
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.os.Build
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
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.SlidingTapListener
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.webview.DarkModeApplier
import jp.toastkid.yobidashi.browser.webview.WebViewFactory
import jp.toastkid.yobidashi.databinding.PopupFloatingPreviewBinding

/**
 * Floating preview.
 *
 * @author toastkidjp
 */
class FloatingPreview(context: Context) {

    private val popupWindow = PopupWindow(context)

    private val webView = WebViewFactory().make(context)

    private val resources = context.resources

    private val heightPixels = resources.displayMetrics.heightPixels

    private val headerHeight =
            resources.getDimensionPixelSize(R.dimen.floating_preview_header_height)

    private val swipeLimit = heightPixels - (headerHeight / 2)

    private val binding: PopupFloatingPreviewBinding

    private val darkModeApplier = DarkModeApplier()

    private var viewModel: FloatingPreviewViewModel? = null

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = DataBindingUtil.inflate(layoutInflater, LAYOUT_ID, null, false)
        binding.preview = this
        binding.previewContainer.addView(webView)

        popupWindow.contentView = binding.root

        (context as? FragmentActivity)?.also {
            viewModel = ViewModelProvider(context).get(FloatingPreviewViewModel::class.java)

            viewModel?.title?.observe(context, Observer {
                binding.title.text = it
            })

            viewModel?.icon?.observe(context, Observer {
                binding.icon.setImageBitmap(it)
            })

            viewModel?.url?.observe(context, Observer {
                binding.url.text = it
            })

            viewModel?.progress?.observe(context, Observer(::setNewProgress))
        }

        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        popupWindow.setOnDismissListener {
            extractWebView()?.also {
                it.isEnabled = false
                it.onPause()
            }
        }

        popupWindow.width = WindowManager.LayoutParams.MATCH_PARENT
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT

        popupWindow.isClippingEnabled = false
        popupWindow.isOutsideTouchable = false

        popupWindow.animationStyle = R.style.PopupWindowVisibilityAnimation

        WebViewInitializer()(webView)

        setSlidingListener()

        onResume()
    }

    fun onResume() {
        val context = binding.root.context
        val preferenceApplier = PreferenceApplier(context)
        binding.previewContainer.foreground =
                ColorDrawable(preferenceApplier.filterColor(ContextCompat.getColor(context, R.color.default_color_filter)))
        binding.progress.progressDrawable.colorFilter =
                PorterDuffColorFilter(
                        PreferenceApplier(context).fontColor,
                        PorterDuff.Mode.SRC_IN
                )
        webView.onResume()
    }

    fun onPause() {
        webView.onPause()
    }

    /**
     * Invoke floating preview.
     *
     * @param parent Parent [View] for Snackbar
     * @param url URL string
     */
    fun show(parent: View, url: String) {
        binding.icon.setImageBitmap(null)

        darkModeApplier(webView, PreferenceApplier(webView.context).useDarkMode())
        webView.isEnabled = true
        webView.onResume()

        webView.loadUrl(url)

        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, -600)
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
        val slidingTouchListener = SlidingTapListener(binding.contentPanel)
        slidingTouchListener.setCallback(object : SlidingTapListener.OnNewPosition {
            override fun onNewPosition(x: Float, y: Float) {
                if (y > swipeLimit) {
                    return
                }
                popupWindow.update(-1, -(y.toInt() - headerHeight), -1, -1)
            }
        })
        binding.header.setOnTouchListener(slidingTouchListener)
    }

    /**
     * Open passed url by new tab.
     *
     * @param url URL string
     */
    private fun openNewTabWithUrl(url: String) {
        (binding.root.context as? FragmentActivity)?.also { fragmentActivity ->
            ViewModelProvider(fragmentActivity)
                    .get(BrowserViewModel::class.java)
                    .open(url.toUri())
        }

        hide()
    }

    private fun extractWebView(): WebView? {
        if (binding.previewContainer.isEmpty()) {
            return null
        }
        val view = binding.previewContainer[0]
        return view as? WebView
    }

    private fun setNewProgress(newProgress: Int) {
        if (newProgress > 85) {
            if (binding.progress.isVisible) {
                binding.progress.isGone = true
            }
            return
        }

        if (binding.progress.isGone) {
            binding.progress.isVisible = true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.progress.setProgress(newProgress, true)
        } else {
            binding.progress.progress = newProgress
        }
    }

    private fun isVisible() = popupWindow.isShowing

    fun dispose() {
        webView.destroy()
    }

    fun onBackPressed(): Boolean {
        if (popupWindow.isShowing.not()) {
            return false
        }
        if (webView.canGoBack()) {
            webView.goBack()
            return true
        }
        if (isVisible()) {
            hide()
            return true
        }
        return false
    }

    companion object {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.popup_floating_preview

    }
}