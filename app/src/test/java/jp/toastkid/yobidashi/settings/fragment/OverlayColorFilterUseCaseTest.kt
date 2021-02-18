/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings.fragment

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.preference.PreferenceApplier
import org.junit.After
import org.junit.Before
import org.junit.Test

class OverlayColorFilterUseCaseTest {

    private lateinit var overlayColorFilterUseCase: OverlayColorFilterUseCase

    @MockK
    private lateinit var preferenceApplier: PreferenceApplier

    @MockK
    private lateinit var colorResolver: (Int) -> Int

    @MockK
    private lateinit var overlayColorFilterViewModel: OverlayColorFilterViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { colorResolver.invoke(any()) }.returns(Color.BLACK)

        mockkStatic(ColorUtils::class)
        every { ColorUtils.setAlphaComponent(any(), any()) }.returns(Color.BLACK)
        every { preferenceApplier.filterColor(any()) }.returns(Color.TRANSPARENT)
        every { preferenceApplier.setFilterColor(any()) }.answers { Unit }
        every { overlayColorFilterViewModel.newColor(any()) }.answers { Unit }

        overlayColorFilterUseCase =
                OverlayColorFilterUseCase(preferenceApplier, colorResolver, overlayColorFilterViewModel)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun setBlue() {
        overlayColorFilterUseCase.setBlue()

        verify(atLeast = 1) { colorResolver.invoke(any()) }
        verify(exactly = 1) { ColorUtils.setAlphaComponent(any(), any()) }
        verify(exactly = 1) { preferenceApplier.filterColor(any()) }
        verify(exactly = 1) { preferenceApplier.setFilterColor(any()) }
        verify(exactly = 1) { overlayColorFilterViewModel.newColor(any()) }
    }

    @Test
    fun setRed() {
        overlayColorFilterUseCase.setRed()

        verify(atLeast = 1) { colorResolver.invoke(any()) }
        verify(exactly = 1) { ColorUtils.setAlphaComponent(any(), any()) }
        verify(exactly = 1) { preferenceApplier.filterColor(any()) }
        verify(exactly = 1) { preferenceApplier.setFilterColor(any()) }
        verify(exactly = 1) { overlayColorFilterViewModel.newColor(any()) }
    }

    @Test
    fun setYellow() {
        overlayColorFilterUseCase.setYellow()

        verify(atLeast = 1) { colorResolver.invoke(any()) }
        verify(exactly = 1) { ColorUtils.setAlphaComponent(any(), any()) }
        verify(exactly = 1) { preferenceApplier.filterColor(any()) }
        verify(exactly = 1) { preferenceApplier.setFilterColor(any()) }
        verify(exactly = 1) { overlayColorFilterViewModel.newColor(any()) }
    }

    @Test
    fun setOrange() {
        overlayColorFilterUseCase.setOrange()

        verify(atLeast = 1) { colorResolver.invoke(any()) }
        verify(exactly = 1) { ColorUtils.setAlphaComponent(any(), any()) }
        verify(exactly = 1) { preferenceApplier.filterColor(any()) }
        verify(exactly = 1) { preferenceApplier.setFilterColor(any()) }
        verify(exactly = 1) { overlayColorFilterViewModel.newColor(any()) }
    }

    @Test
    fun setRedYellow() {
        overlayColorFilterUseCase.setRedYellow()

        verify(atLeast = 1) { colorResolver.invoke(any()) }
        verify(exactly = 1) { ColorUtils.setAlphaComponent(any(), any()) }
        verify(exactly = 1) { preferenceApplier.filterColor(any()) }
        verify(exactly = 1) { preferenceApplier.setFilterColor(any()) }
        verify(exactly = 1) { overlayColorFilterViewModel.newColor(any()) }
    }

    @Test
    fun testSetGreen() {
        overlayColorFilterUseCase.setGreen()

        verify(atLeast = 1) { colorResolver.invoke(any()) }
        verify(exactly = 1) { ColorUtils.setAlphaComponent(any(), any()) }
        verify(exactly = 1) { preferenceApplier.filterColor(any()) }
        verify(exactly = 1) { preferenceApplier.setFilterColor(any()) }
        verify(exactly = 1) { overlayColorFilterViewModel.newColor(any()) }
    }

    @Test
    fun testSetDark() {
        overlayColorFilterUseCase.setDark()

        verify(atLeast = 1) { colorResolver.invoke(any()) }
        verify(exactly = 1) { ColorUtils.setAlphaComponent(any(), any()) }
        verify(exactly = 1) { preferenceApplier.filterColor(any()) }
        verify(exactly = 1) { preferenceApplier.setFilterColor(any()) }
        verify(exactly = 1) { overlayColorFilterViewModel.newColor(any()) }
    }

    @Test
    fun testSetAlpha() {
        overlayColorFilterUseCase.setAlpha(120)

        verify(atLeast = 1) { colorResolver.invoke(any()) }
        verify(exactly = 1) { ColorUtils.setAlphaComponent(any(), any()) }
        verify(exactly = 1) { preferenceApplier.filterColor(any()) }
        verify(exactly = 1) { preferenceApplier.setFilterColor(any()) }
        verify(exactly = 1) { overlayColorFilterViewModel.newColor(any()) }
    }

    @Test
    fun testSetDefault() {
        overlayColorFilterUseCase.setDefault()

        verify(atLeast = 1) { colorResolver.invoke(any()) }
        verify(exactly = 1) { ColorUtils.setAlphaComponent(any(), any()) }
        verify(exactly = 0) { preferenceApplier.filterColor(any()) }
        verify(exactly = 1) { preferenceApplier.setFilterColor(any()) }
        verify(exactly = 1) { overlayColorFilterViewModel.newColor(any()) }
    }

}