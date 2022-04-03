/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.webview.usecase

import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.rss.suggestion.RssAddingSuggestion
import jp.toastkid.yobidashi.browser.BrowserHeaderViewModel
import jp.toastkid.yobidashi.browser.FaviconApplier
import jp.toastkid.yobidashi.browser.LoadingViewModel
import jp.toastkid.yobidashi.browser.ScreenMode
import jp.toastkid.yobidashi.browser.block.AdRemover
import jp.toastkid.yobidashi.browser.webview.CustomViewSwitcher
import jp.toastkid.yobidashi.browser.webview.DarkModeApplier
import jp.toastkid.yobidashi.browser.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.browser.webview.WebSettingApplier
import jp.toastkid.yobidashi.browser.webview.WebViewFactoryUseCase
import jp.toastkid.yobidashi.browser.webview.WebViewStateUseCase
import jp.toastkid.yobidashi.browser.webview.factory.WebChromeClientFactory
import jp.toastkid.yobidashi.browser.webview.factory.WebViewClientFactory

class WebViewAssignmentUseCase private constructor(
    private val context: ComponentActivity
    ) {

    operator fun invoke(tabId: String?): View {
        val viewModelProvider = ViewModelProvider(context)
        val browserHeaderViewModel = viewModelProvider.get(BrowserHeaderViewModel::class.java)
        val loadingViewModel = viewModelProvider.get(LoadingViewModel::class.java)
        val contentViewModel = viewModelProvider.get(ContentViewModel::class.java)

        val preferenceApplier = PreferenceApplier(context)
        val faviconApplier = FaviconApplier(context)

        val webViewFactoryUseCase = WebViewFactoryUseCase(
            webViewClientFactory = WebViewClientFactory(
                contentViewModel,
                AdRemover.make(context.assets),
                faviconApplier,
                preferenceApplier,
                browserHeaderViewModel,
                RssAddingSuggestion(preferenceApplier),
                loadingViewModel
            ) { currentView() },
            webChromeClientFactory = WebChromeClientFactory(
                browserHeaderViewModel,
                faviconApplier,
                CustomViewSwitcher({ context }, { currentView() })
            )
        )

        if (!GlobalWebViewPool.containsKey(tabId) && tabId != null) {
            GlobalWebViewPool.put(tabId, webViewFactoryUseCase(context))
        }
        val webView = GlobalWebViewPool.get(tabId)
        WebViewStateUseCase.make(context).restore(webView, tabId)
        webView?.let {
            WebSettingApplier(preferenceApplier).invoke(it.settings)
            it.onResume()
            (it.parent as? ViewGroup)?.removeAllViews()
            DarkModeApplier()(it, preferenceApplier.useDarkMode())
            browserHeaderViewModel.setBackButtonIsEnabled(it.canGoBack())
            browserHeaderViewModel.setForwardButtonIsEnabled(it.canGoForward())
            browserHeaderViewModel.nextTitle(it.title)
            browserHeaderViewModel.nextUrl(it.url)
            if (ScreenMode.find(preferenceApplier.browserScreenMode()) != ScreenMode.FULL_SCREEN) {
                ViewModelProvider(context).get(AppBarViewModel::class.java).show()
            }
        }

        //webViewContainer?.startAnimation(slideUpFromBottom)

        return webView ?: View(context)
    }

    private fun currentView(): WebView? {
        return GlobalWebViewPool.getLatest()
    }

    companion object {

        fun from(context: ComponentActivity) =
            WebViewAssignmentUseCase(context)

    }

}