/*
 * Copyright (c) 2020 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.web.webview.factory

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.web.webview.AlphaConverter
import jp.toastkid.web.webview.CustomWebView
import jp.toastkid.web.webview.WebSettingApplier
import timber.log.Timber

/**
 * [WebView] factory.
 *
 * @author toastkidjp
 */
class WebViewFactory {

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
        webView.setBackgroundColor(Color.Transparent.toArgb())

        val preferenceApplier = PreferenceApplier(context)

        WebSettingApplier(preferenceApplier).invoke(webView.settings)

        webView.isNestedScrollingEnabled = true
        webView.setBackgroundColor(alphaConverter.readBackground(context))

        webView.setDownloadListener { url, _, _, mimeType, _ ->
            when (mimeType) {
                "application/pdf" -> {
                    val intent = Intent().also { it.data = url.toUri() }
                    val currentContext = webView.context
                    intent.action = Intent.ACTION_VIEW
                    currentContext.startActivity(intent)
                }
                else -> {
                    (context as? ViewModelStoreOwner)?.let {
                        ViewModelProvider(it).get(ContentViewModel::class.java).download(url)
                    }
                }
            }
        }

        if (WebViewFeature.isFeatureSupported(WebViewFeature.START_SAFE_BROWSING)) {
            WebViewCompat.startSafeBrowsing(context) { success ->
                if (!success) {
                    Timber.d("Unable to initialize Safe Browsing!")
                }
            }
        }

        return webView
    }

}
