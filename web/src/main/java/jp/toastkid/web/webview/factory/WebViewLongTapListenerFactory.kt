/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.web.webview.factory

import android.os.Handler
import android.os.Looper
import android.view.View.OnLongClickListener
import android.webkit.WebView
import jp.toastkid.web.webview.CustomWebView

class WebViewLongTapListenerFactory {

    private val longTapItemHolder = LongTapItemHolder()

    /**
     * Use for only extract anchor URL.
     */
    private val handler = Handler(Looper.getMainLooper()) {
        it.data?.let(longTapItemHolder::extract)
        true
    }

    operator fun invoke(
        showImageAnchorDialog: (String?, String?, String?) -> Unit
    ): OnLongClickListener {
        return OnLongClickListener { webView ->
            if ((webView as? CustomWebView)?.enablePullToRefresh == true) {
                return@OnLongClickListener true
            }

            if (webView !is WebView) {
                return@OnLongClickListener true
            }

            val hitResult = webView.hitTestResult
            when (hitResult.type) {
                WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                    val url = hitResult.extra ?: return@OnLongClickListener false
                    webView.requestFocusNodeHref(handler.obtainMessage())
                    if (longTapItemHolder.anchor.isEmpty()) {
                        handler.postDelayed({
                            showImageAnchorDialog(
                                longTapItemHolder.title,
                                longTapItemHolder.anchor,
                                url
                            )
                            longTapItemHolder.reset()
                        }, 300L)

                        return@OnLongClickListener true
                    }
                    showImageAnchorDialog(longTapItemHolder.title, longTapItemHolder.anchor, url)
                    false
                }
                WebView.HitTestResult.IMAGE_TYPE -> {
                    val url = hitResult.extra ?: return@OnLongClickListener false
                    showImageAnchorDialog(
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
                            showImageAnchorDialog(
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
    }

}