/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.webview

import android.os.Build
import android.webkit.WebView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

/**
 * @author toastkidjp
 */
class DarkModeApplier {

    operator fun invoke(webView: WebView, useDarkMode: Boolean) {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            && WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.settings, true)
        }

        val forceDarkMode =
            if (useDarkMode) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF
        WebSettingsCompat.setForceDark(webView.settings, forceDarkMode)
    }

}