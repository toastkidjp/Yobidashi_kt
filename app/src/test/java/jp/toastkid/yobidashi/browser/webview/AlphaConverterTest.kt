/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.webview

import android.content.Context
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.preference.PreferenceApplier
import org.junit.After
import org.junit.Before
import org.junit.Test

class AlphaConverterTest {

    private lateinit var alphaConverter: AlphaConverter

    @MockK
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        alphaConverter = AlphaConverter()

        mockkStatic(ColorUtils::class)
        every { ColorUtils.setAlphaComponent(any(), any()) }.answers { Color.WHITE }

        mockkConstructor(PreferenceApplier::class)
        every { anyConstructed<PreferenceApplier>().getWebViewBackgroundAlpha() }.returns(0.3f)

        every { context.getSharedPreferences(any(), any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testReadBackground() {
        alphaConverter.readBackground(context)

        verify(atLeast = 1) { context.getSharedPreferences(any(), any()) }
        verify(atLeast = 1) { ColorUtils.setAlphaComponent(any(), any()) }
        verify(atLeast = 1) { anyConstructed<PreferenceApplier>().getWebViewBackgroundAlpha() }
    }

}