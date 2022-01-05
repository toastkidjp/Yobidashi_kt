/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.about.license

import android.content.Context
import android.content.res.AssetManager
import android.view.View
import android.webkit.WebView
import android.widget.FrameLayout
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class LicenseHtmlLoaderUseCaseTest {

    @InjectMockKs
    private lateinit var licenseHtmlLoaderUseCase: LicenseHtmlLoaderUseCase

    @MockK
    private lateinit var frameLayout: FrameLayout

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var assetManager: AssetManager

    @MockK
    private lateinit var webView: WebView

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { frameLayout.addView(any()) }.just(Runs)
        every { frameLayout.getChildAt(any()) }.returns(webView)
        every { frameLayout.context }.returns(context)
        every { frameLayout.visibility = any() }.just(Runs)
        every { frameLayout.visibility }.returns(View.VISIBLE)
        every { frameLayout.childCount }.returns(0)
        every { context.assets }.returns(assetManager)
        every { webView.loadDataWithBaseURL(any(), any(), any(), any(), any()) }.just(Runs)
        every { webView.scrollTo(any(), any()) }.just(Runs)

        mockkConstructor(LicenseContentLoaderUseCase::class)
        every { anyConstructed<LicenseContentLoaderUseCase>().invoke() }.returns("<title>test</title>")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        licenseHtmlLoaderUseCase.invoke(frameLayout)

        verify(exactly = 1) { frameLayout.addView(any()) }
        verify(atLeast = 1) { frameLayout.context }
        verify(atLeast = 1) { frameLayout.visibility }
        verify(atLeast = 1) { frameLayout.visibility = any() }
        verify(atLeast = 1) { frameLayout.childCount }
        verify(exactly = 1) { context.assets }
        verify(exactly = 1) { webView.loadDataWithBaseURL(any(), any(), any(), any(), any()) }
        verify(exactly = 0) { webView.scrollTo(any(), any()) }
        verify(exactly = 1) { anyConstructed<LicenseContentLoaderUseCase>().invoke() }
    }

    @Test
    fun testContainerIsGoneCase() {
        every { frameLayout.visibility }.returns(View.GONE)

        licenseHtmlLoaderUseCase.invoke(frameLayout)

        verify(exactly = 0) { frameLayout.addView(any()) }
        verify(exactly = 0) { frameLayout.context }
        verify(atLeast = 1) { frameLayout.visibility }
        verify(atLeast = 1) { frameLayout.visibility = any() }
        verify(exactly = 1) { frameLayout.getChildAt(0) }
        verify(exactly = 0) { frameLayout.childCount }
        verify(exactly = 0) { context.assets }
        verify(exactly = 0) { webView.loadDataWithBaseURL(any(), any(), any(), any(), any()) }
        verify(exactly = 1) { webView.scrollTo(0, 0) }
        verify(exactly = 0) { anyConstructed<LicenseContentLoaderUseCase>().invoke() }
    }

}