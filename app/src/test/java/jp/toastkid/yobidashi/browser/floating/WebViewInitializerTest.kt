/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.floating

import android.webkit.WebView
import androidx.fragment.app.FragmentActivity
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.browser.block.AdRemover
import org.junit.After
import org.junit.Before
import org.junit.Test

class WebViewInitializerTest {

    @InjectMockKs
    private lateinit var webViewInitializer: WebViewInitializer

    @MockK
    private lateinit var preferenceApplier: PreferenceApplier

    @MockK
    private lateinit var viewModel: FloatingPreviewViewModel

    @MockK
    private lateinit var webView: WebView

    @MockK
    private lateinit var fragmentActivity: FragmentActivity

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { fragmentActivity.getAssets() }.returns(mockk())

        every { webView.getContext() }.returns(fragmentActivity)

        mockkObject(AdRemover)
        every { AdRemover.make(any()) }.returns(mockk())

        every { webView.setWebViewClient(any()) }.answers { Unit }
        every { webView.setWebChromeClient(any()) }.answers { Unit }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        webViewInitializer.invoke(webView)

        verify(exactly = 1) { fragmentActivity.getAssets() }
        verify(exactly = 1) { webView.getContext() }
        verify(exactly = 1) { AdRemover.make(any()) }
        verify(exactly = 1) { webView.setWebViewClient(any()) }
        verify(exactly = 1) { webView.setWebChromeClient(any()) }
    }

}