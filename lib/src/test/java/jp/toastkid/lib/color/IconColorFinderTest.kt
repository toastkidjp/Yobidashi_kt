/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.color

import android.content.res.Configuration
import android.graphics.Color
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.night.DisplayMode
import jp.toastkid.lib.preference.PreferenceApplier
import org.junit.After
import org.junit.Before
import org.junit.Test

class IconColorFinderTest {

    @InjectMockKs
    private lateinit var iconColorFinder: IconColorFinder

    @MockK
    private lateinit var configuration: Configuration

    @MockK
    private lateinit var preferenceApplier: PreferenceApplier

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkConstructor(DisplayMode::class)
        every { preferenceApplier.fontColor }.returns(Color.BLACK)
        every { preferenceApplier.color }.returns(Color.WHITE)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun test() {
        every { anyConstructed<DisplayMode>().isNightMode() }.returns(true)

        iconColorFinder.invoke()

        verify (exactly = 1) { anyConstructed<DisplayMode>().isNightMode() }
        verify (exactly = 1) { preferenceApplier.fontColor }
        verify (exactly = 0) { preferenceApplier.color }
    }

}