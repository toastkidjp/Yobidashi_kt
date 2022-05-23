/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.usecase

import android.view.ViewGroup
import android.view.animation.Animation
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.browser.ScreenMode
import jp.toastkid.yobidashi.browser.webview.DarkModeApplier
import jp.toastkid.yobidashi.browser.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.browser.webview.WebSettingApplier
import jp.toastkid.yobidashi.browser.webview.WebViewStateUseCase

/**
 * @author toastkidjp
 */
class WebViewReplacementUseCase(
    private val webViewContainer: FrameLayout?,
    private val webViewStateUseCase: WebViewStateUseCase,
    private val makeWebView: () -> WebView,
    private val browserViewModel: BrowserViewModel?,
    private val preferenceApplier: PreferenceApplier,
    private val slideUpFromBottom: Animation,
    private val darkThemeApplier: DarkModeApplier = DarkModeApplier()
) {

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

            //webViewContainer?.startAnimation(slideUpFromBottom)

            val activity = webViewContainer?.context
            if (activity is ViewModelStoreOwner
                    && ScreenMode.find(preferenceApplier.browserScreenMode()) != ScreenMode.FULL_SCREEN) {
                ViewModelProvider(activity).get(AppBarViewModel::class.java).show()
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