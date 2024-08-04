/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.web.usecase

import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.web.ScreenMode
import jp.toastkid.web.view.WebTabUiViewModel
import jp.toastkid.web.webview.DarkModeApplier
import jp.toastkid.web.webview.GlobalWebViewPool
import jp.toastkid.web.webview.WebSettingApplier
import jp.toastkid.web.webview.WebViewStateUseCase

/**
 * @author toastkidjp
 */
class WebViewReplacementUseCase(
    private val webViewContainer: FrameLayout?,
    private val webViewStateUseCase: WebViewStateUseCase,
    private val makeWebView: () -> WebView,
    private val browserViewModel: WebTabUiViewModel?,
    private val preferenceApplier: PreferenceApplier,
    private val darkThemeApplier: DarkModeApplier = DarkModeApplier()
) {

    private val noopWebViewClient = object : WebViewClient() {}

    /**
     *
     * @param tabId Tab's ID
     * @return If return true, it should load immediately
     */
    operator fun invoke(tabId: String): Boolean {
        val currentWebView = getWebView(tabId)
        if (webViewContainer?.childCount != 0) {
            val previousView = webViewContainer?.get(0)
            if (currentWebView == previousView) {
                return false
            }

            if (previousView is WebView) {
                previousView.webViewClient = noopWebViewClient
                previousView.webChromeClient = null
                previousView.setOnLongClickListener(null)
            }
        }

        setWebView(currentWebView)
        return currentWebView?.url.isNullOrBlank()
    }

    private fun setWebView(webView: WebView?) {
        webViewContainer?.removeAllViews()
        webView?.let {
            it.onResume()
            (it.parent as? ViewGroup)?.removeAllViews()
            darkThemeApplier(it, preferenceApplier.useDarkMode())
            webViewContainer?.addView(it)
            browserViewModel?.setBackButtonIsEnabled(it.canGoBack())
            browserViewModel?.setForwardButtonIsEnabled(it.canGoForward())
            browserViewModel?.nextTitle(it.title)
            browserViewModel?.nextUrl(it.url)

            val activity = webViewContainer?.context
            if (activity is ViewModelStoreOwner
                    && ScreenMode.find(preferenceApplier.browserScreenMode()) != ScreenMode.FULL_SCREEN) {
                ViewModelProvider(activity)[ContentViewModel::class.java].showAppBar()
            }
        }

        reloadWebViewSettings()
    }

    /**
     * Get [WebView] with tab ID.
     *
     * @param tabId Tab's ID.
     * @return [WebView]
     */
    private fun getWebView(tabId: String?): WebView? {
        if (!GlobalWebViewPool.containsKey(tabId) && tabId != null) {
            GlobalWebViewPool.put(tabId, makeWebView())
        }
        val webView = GlobalWebViewPool.get(tabId)
        webViewStateUseCase.restore(webView, tabId)
        return webView
    }

    /**
     * Reload [WebSettings].
     */
    private fun reloadWebViewSettings() {
        WebSettingApplier(preferenceApplier)
                .invoke(GlobalWebViewPool.getLatest()?.settings)
    }

}