/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.webview.usecase

import android.webkit.WebView
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class DarkCssInjectorUseCaseTest {

    @InjectMockKs
    private lateinit var darkCssInjectorUseCase: DarkCssInjectorUseCase

    @MockK
    private lateinit var webView: WebView

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { webView.evaluateJavascript(any(), any()) }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        darkCssInjectorUseCase.invoke(webView)

        verify { webView.evaluateJavascript(any(), any()) }
    }

    @Test
    fun testNull() {
        darkCssInjectorUseCase.invoke(null)

        verify(inverse = true) { webView.evaluateJavascript(any(), any()) }
    }

}