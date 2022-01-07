/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.about.license

import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import jp.toastkid.about.R
import java.nio.charset.StandardCharsets

/**
 * @author toastkidjp
 */
internal class LicenseHtmlLoaderUseCase {

    operator fun invoke(container: FrameLayout) {
        val content = LicenseContentLoaderUseCase(container.context.assets).invoke()

        val webView = WebView(container.context)
        webView.settings.also {
            it.javaScriptCanOpenWindowsAutomatically = false
            it.javaScriptEnabled = false
            it.blockNetworkLoads = false
            it.databaseEnabled = false
            it.domStorageEnabled = false
            it.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        }

        AlertDialog.Builder(container.context)
            .setTitle(R.string.title_licenses)
            .setView(webView)
            .setPositiveButton(R.string.ok, { d, i -> d.dismiss() })
            .show()

        webView.loadDataWithBaseURL(
            null,
            content,
            MIMETYPE,
            encoding,
            null
        )
    }

    companion object {

        private const val MIMETYPE = "text/html"

        private val encoding = StandardCharsets.UTF_8.name()

    }

}