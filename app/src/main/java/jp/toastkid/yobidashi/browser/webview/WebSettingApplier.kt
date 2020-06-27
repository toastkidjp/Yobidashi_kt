/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.webview

import android.os.Build
import android.webkit.WebSettings
import jp.toastkid.yobidashi.browser.user_agent.UserAgent
import jp.toastkid.lib.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
class WebSettingApplier(private val preferenceApplier: PreferenceApplier) {

    operator fun invoke(webSettings: WebSettings?) {
        if (webSettings == null) {
            return
        }

        webSettings.javaScriptEnabled = preferenceApplier.useJavaScript()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            webSettings.saveFormData = preferenceApplier.doesSaveForm()
        }
        webSettings.loadsImagesAutomatically = preferenceApplier.doesLoadImage()
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webSettings.javaScriptCanOpenWindowsAutomatically = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webSettings.safeBrowsingEnabled = true
        }
        webSettings.setSupportMultipleWindows(true)
        webSettings.domStorageEnabled = true

        val ua =  UserAgent.valueOf(preferenceApplier.userAgent()).text()

        if (webSettings.userAgentString != ua) {
            webSettings.userAgentString = ua
        }
    }
}