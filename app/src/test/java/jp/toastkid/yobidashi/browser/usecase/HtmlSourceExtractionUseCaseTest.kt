/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.usecase

import android.webkit.WebView
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class HtmlSourceExtractionUseCaseTest {

    @MockK
    private lateinit var webView: WebView

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun test() {
        every { webView.url }.returns("https://www.yahoo.co.jp")
        every { webView.evaluateJavascript(any(), any()) }.just(Runs)

        HtmlSourceExtractionUseCase().invoke(webView, mockk())

        verify (exactly = 1) { webView.evaluateJavascript(any(), any()) }
    }

    @Test
    fun testExceptingCase() {
        every { webView.url }.returns("https://accounts.google.com/signin")
        every { webView.evaluateJavascript(any(), any()) }.just(Runs)

        HtmlSourceExtractionUseCase().invoke(webView, mockk())

        verify (exactly = 0) { webView.evaluateJavascript(any(), any()) }
    }

}