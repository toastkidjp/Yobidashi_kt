/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.usecase

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView

internal class PrintCurrentPageUseCase {

    operator fun invoke(webView: WebView?) {
        webView ?: return
        val adapter = webView.createPrintDocumentAdapter("${webView.title}.pdf")
        val printManager = webView.context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        printManager?.print(PRINTER_NAME, adapter, PrintAttributes.Builder().build())
    }

}

private const val PRINTER_NAME = "PrintPDF"
