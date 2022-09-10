/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.webview

import android.content.res.Configuration
import android.os.Build
import android.webkit.WebView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

/**
 * @author toastkidjp
 */
class DarkModeApplier {

    operator fun invoke(webView: WebView, useDarkMode: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            && WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.settings, useDarkMode)
        }

        if (useDarkMode) {
            webView.context.resources.configuration.uiMode =
                webView.context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        }
    }

}