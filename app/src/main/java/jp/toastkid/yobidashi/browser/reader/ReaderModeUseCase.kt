/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.reader

import android.os.Build
import android.webkit.ValueCallback
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.annotation.UiThread

/**
 * @author toastkidjp
 */
class ReaderModeUseCase {

    private val script = """
        var ps = document.getElementsByTagName('p');
        var content = "";
        for (var i = 0; i < ps.length; i++) {
            content = content + ps[i].textContent;
        }
        content;
    """.trimIndent()

    @UiThread
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    operator fun invoke(webView: WebView?, callback: ValueCallback<String>) {
        webView?.evaluateJavascript(script, callback)
    }
}