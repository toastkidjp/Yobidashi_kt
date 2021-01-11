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
    }

    @Test
    fun testPassNull() {
        webSettingApplier.invoke(null)

        verify (exactly = 0) { preferenceApplier.useJavaScript() }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}