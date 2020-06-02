/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.webview

import android.webkit.WebView
import androidx.annotation.ColorInt

/**
 * Implements for retaining [WebView] instances on global.
 *
 * @author toastkidjp
 */
object GlobalWebViewPool {

    private val pool = WebViewPool()

    fun get(tabId: String?): WebView? = pool.get(tabId)

    fun getLatest(): WebView? = pool.getLatest()

    fun put(tabId: String, webView: WebView) = pool.put(tabId, webView)

    fun containsKey(tabId: String?) = pool.containsKey(tabId)

    fun remove(tabId: String?) = pool.remove(tabId)

    fun resize(newSize: Int) = pool.resize(newSize)

    fun applyNewAlpha(@ColorInt newAlphaBackground: Int) = pool.applyNewAlpha(newAlphaBackground)

    fun onResume() = pool.onResume()

    fun onPause() = pool.onPause()

    fun dispose() = pool.dispose()
}