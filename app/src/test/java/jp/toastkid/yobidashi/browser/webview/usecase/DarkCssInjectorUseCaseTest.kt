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
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify

class DarkCssInjectorUseCaseTest {

    @InjectMockKs
    private lateinit var darkCssInjectorUseCase: DarkCssInjectorUseCase

    @MockK
    private lateinit var webView: WebView

    @org.junit.Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { webView.evaluateJavascript(any(), any()) }.answers { Unit }
    }

    @org.junit.After
    fun tearDown() {
        unmockkAll()
    }

    @org.junit.Test
    fun invoke() {
        darkCssInjectorUseCase.invoke(webView)

        verify { webView.evaluateJavascript(any(), any()) }
    }
}