/*
 * Copyright (c) 2020 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.webview.factory

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.WebView
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.browser.webview.AlphaConverter
import jp.toastkid.yobidashi.browser.webview.CustomWebView
import jp.toastkid.yobidashi.browser.webview.WebSettingApplier

/**
 * [WebView] factory.
 *
 * @author toastkidjp
 */
internal class WebViewFactory {

    /**
     * Use for only extract anchor URL.
     */
    private val handler = Handler(Looper.getMainLooper()) {
        it.data?.let(longTapItemHolder::extract)
        true
    }

    private val longTapItemHolder = LongTapItemHolder()

    /**
     * Color alpha converter.
     */
    private val alphaConverter = AlphaConverter()

    /**
     * Make new [WebView].
     *
     * @param context [Context]
     * @return [CustomWebView]
     */
    @SuppressLint("ClickableViewAccessibility")
    fun make(context: Context): CustomWebView {
        val webView = CustomWebView(context)
        webView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )

        val preferenceApplier = PreferenceApplier(context)

        webView.setOnLongClickListener {
            val hitResult = webView.hitTestResult
            when (hitResult.type) {
                WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                    val url = hitResult.extra ?: return@setOnLongClickListener false
                    webView.requestFocusNodeHref(handler.obtainMessage())
                    if (longTapItemHolder.anchor.isEmpty()) {
                        handler.postDelayed({
                            showImageAnchorDialog(url, context)
                            longTapItemHolder.reset()
                        }, 300L)

                        return@setOnLongClickListener true
                    }
                    showImageAnchorDialog(url, context)
                    false
                }
                WebView.HitTestResult.IMAGE_TYPE -> {
                    val url = hitResult.extra ?: return@setOnLongClickListener false
                    extractViewModel(context)?.setLongTapParameters(
                        longTapItemHolder.title,
                        longTapItemHolder.anchor,
                        url
                    )
                    true
                }
                WebView.HitTestResult.SRC_ANCHOR_TYPE -> {
                    webView.requestFocusNodeHref(handler.obtainMessage())

                    handler.postDelayed(
                        {
                            extractViewModel(context)?.setLongTapParameters(
                                longTapItemHolder.title,
                                longTapItemHolder.anchor,
                                null
                            )
                            longTapItemHolder.reset()
                        },
                        300L
                    )
                    false
                }
                else -> {
                    false
                }
            }
        }

        WebSettingApplier(preferenceApplier).invoke(webView.settings)

        webView.isNestedScrollingEnabled = true
        webView.setBackgroundColor(alphaConverter.readBackground(context))

        webView.setDownloadListener { url, _, _, mimeType, _ ->
            when (mimeType) {
                "application/pdf" -> {
                    val intent = Intent().also { it.data = url.toUri() }
                    val currentContext = webView.context
                    if (currentContext.packageManager.resolveActivity(intent, 0) == null) {
                        intent.action = Intent.ACTION_VIEW
                    }
                    currentContext.startActivity(intent)
                }
                else -> {
                    (context as? ViewModelStoreOwner)?.let {
                        ViewModelProvider(it).get(BrowserViewModel::class.java).download(url)
                    }
                }
            }
        }

        return webView
    }

    /**
     * Show image anchor type dialog.
     *
     * @param imageUrl URL string
     * @param context [Context]
     */
    private fun showImageAnchorDialog(imageUrl: String, context: Context) {
        extractViewModel(context)?.setLongTapParameters(
            longTapItemHolder.title,
            longTapItemHolder.anchor,
            imageUrl
        )
    }

    private fun extractViewModel(context: Context?) =
        (context as? ViewModelStoreOwner)?.let {
            ViewModelProvider(it).get(BrowserViewModel::class.java)
        }

}
