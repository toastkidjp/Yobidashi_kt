/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.tab.background

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.yobidashi.browser.webview.CustomWebView
import jp.toastkid.yobidashi.browser.webview.factory.WebChromeClientFactory
import jp.toastkid.yobidashi.browser.webview.factory.WebViewClientFactory
import jp.toastkid.yobidashi.browser.webview.factory.WebViewFactory
import org.junit.After
import org.junit.Before
import org.junit.Test

class BackgroundWebViewFactoryUseCaseTest {

    @InjectMockKs
    private lateinit var backgroundWebViewFactoryUseCase: BackgroundWebViewFactoryUseCase

    @MockK
    private lateinit var webViewFactory: WebViewFactory

    @MockK
    private lateinit var webViewClientFactory: WebViewClientFactory

    @MockK
    private lateinit var webChromeClientFactory: WebChromeClientFactory

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var webView: CustomWebView

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { webView.setWebViewClient(any()) }.answers { Unit }
        every { webView.setWebChromeClient(any()) }.answers { Unit }
        every { webViewFactory.make(any()) }.returns(webView)
        every { webViewClientFactory.invoke() }.returns(mockk())
        every { webChromeClientFactory.invoke() }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun test() {
        backgroundWebViewFactoryUseCase.invoke(context)

        verify(atLeast = 1) { webView.setWebViewClient(any()) }
        verify(atLeast = 1) { webView.setWebChromeClient(any()) }
        verify(atLeast = 1) { webViewFactory.make(any()) }
        verify(atLeast = 1) { webViewClientFactory.invoke() }
        verify(atLeast = 1) { webChromeClientFactory.invoke() }
    }

}