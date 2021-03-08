/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.webview

import android.webkit.WebSettings
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.preference.PreferenceApplier
import org.junit.After
import org.junit.Before
import org.junit.Test

class WebSettingApplierTest {

    private lateinit var webSettingApplier: WebSettingApplier

    @MockK
    private lateinit var preferenceApplier: PreferenceApplier

    @MockK
    private lateinit var webSettings: WebSettings

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        webSettingApplier = WebSettingApplier(preferenceApplier)

        every { preferenceApplier.useJavaScript() }.returns(true)
        every { preferenceApplier.doesSaveForm() }.returns(true)
        every { preferenceApplier.doesLoadImage() }.returns(true)
        every { preferenceApplier.userAgent() }.returns("test")

        every { webSettings.setJavaScriptEnabled(any()) }.answers { Unit }
        every { webSettings.setSaveFormData(any()) }.answers { Unit }
        every { webSettings.setLoadsImagesAutomatically(any()) }.answers { Unit }
        every { webSettings.setBuiltInZoomControls(any()) }.answers { Unit }
        every { webSettings.setDisplayZoomControls(any()) }.answers { Unit }
        every { webSettings.setJavaScriptCanOpenWindowsAutomatically(any()) }.answers { Unit }
        every { webSettings.setSafeBrowsingEnabled(any()) }.answers { Unit }
        every { webSettings.setSupportMultipleWindows(true) }.answers { Unit }
        every { webSettings.setDomStorageEnabled(any()) }.answers { Unit }
        every { webSettings.getUserAgentString() }.returns("test")
        every { webSettings.setUserAgentString(any()) }.answers { Unit }
        every { webSettings.setAllowFileAccess(any()) }.answers { Unit }
    }

    @Test
    fun testPassNull() {
        webSettingApplier.invoke(null)

        verify (exactly = 0) { preferenceApplier.useJavaScript() }
    }

    @Test
    fun testInvoke() {
        webSettingApplier.invoke(webSettings)

        verify (exactly = 1) { preferenceApplier.useJavaScript() }
        verify (exactly = 1) { preferenceApplier.doesSaveForm() }
        verify (exactly = 1) { preferenceApplier.doesLoadImage() }
        verify (exactly = 1) { preferenceApplier.userAgent() }

        verify (exactly = 1) { webSettings.setJavaScriptEnabled(any()) }
        verify (exactly = 1) { webSettings.setSaveFormData(any()) }
        verify (exactly = 1) { webSettings.setLoadsImagesAutomatically(any()) }
        verify (exactly = 1) { webSettings.setBuiltInZoomControls(any()) }
        verify (exactly = 1) { webSettings.setDisplayZoomControls(any()) }
        verify (exactly = 1) { webSettings.setJavaScriptCanOpenWindowsAutomatically(any()) }
        verify (exactly = 1) { webSettings.setSupportMultipleWindows(true) }
        verify (exactly = 1) { webSettings.setDomStorageEnabled(any()) }
        verify (exactly = 1) { webSettings.getUserAgentString() }
        verify (exactly = 1) { webSettings.setUserAgentString(any()) }
        verify (exactly = 1) { webSettings.setAllowFileAccess(any()) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}