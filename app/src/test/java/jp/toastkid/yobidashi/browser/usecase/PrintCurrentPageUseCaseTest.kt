/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.usecase

import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.webkit.WebView
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class PrintCurrentPageUseCaseTest {

    @InjectMockKs
    private lateinit var printCurrentPageUseCase: PrintCurrentPageUseCase

    @MockK
    private lateinit var webView: WebView

    @MockK
    private lateinit var printDocumentAdapter: PrintDocumentAdapter

    @MockK
    private lateinit var printManager: PrintManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { webView.title }.returns("test")
        every { webView.createPrintDocumentAdapter(any()) }.returns(printDocumentAdapter)
        every { webView.context.getSystemService(any()) }.returns(printManager)
        every { printManager.print(any(), any(), any()) }.returns(mockk())

        mockkConstructor(PrintAttributes.Builder::class)
        every { anyConstructed<PrintAttributes.Builder>().build() }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        printCurrentPageUseCase.invoke(webView)

        verify { webView.title }
        verify { webView.createPrintDocumentAdapter(any()) }
        verify { webView.context.getSystemService(any()) }
        verify { printManager.print(any(), any(), any()) }
    }
}