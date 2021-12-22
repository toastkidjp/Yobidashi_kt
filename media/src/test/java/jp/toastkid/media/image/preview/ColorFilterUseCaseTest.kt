/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.media.image.preview

import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class ColorFilterUseCaseTest {

    @InjectMockKs
    private lateinit var colorFilterUseCase: ColorFilterUseCase

    @MockK
    private lateinit var viewModel: ImagePreviewFragmentViewModel

    @Before
    fun setUp() {
        mockkConstructor(ColorMatrix::class)
        every { anyConstructed<ColorMatrix>().set(any<FloatArray>()) }.just(Runs)
        every { anyConstructed<ColorMatrix>().setSaturation(any()) }.just(Runs)

        mockkObject(ImageColorFilter.SEPIA)
        every { ImageColorFilter.SEPIA.filter }.returns(mockk())

        MockKAnnotations.init(this)

        every { viewModel.newColorFilter(any<ColorFilter>()) }.just(Runs)
        every { viewModel.newColorFilter(any<ImageColorFilter>()) }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testApplyAlpha() {
        colorFilterUseCase.applyAlpha(0.5f)

        verify(exactly = 1) { viewModel.newColorFilter(any<ColorFilter>()) }
        verify(exactly = 0) { viewModel.newColorFilter(any<ImageColorFilter>()) }
    }

    @Test
    fun testApplyContrast() {
        colorFilterUseCase.applyContrast(0.5f)

        verify(exactly = 1) { viewModel.newColorFilter(any<ColorFilter>()) }
        verify(exactly = 0) { viewModel.newColorFilter(any<ImageColorFilter>()) }
    }

    @Test
    fun testReverseFilter() {
        colorFilterUseCase.reverseFilter()

        verify(exactly = 0) { viewModel.newColorFilter(any<ColorFilter>()) }
        verify(exactly = 1) { viewModel.newColorFilter(any<ImageColorFilter>()) }
    }

    @Test
    fun testSepia() {
        colorFilterUseCase.sepia()

        verify(exactly = 0) { viewModel.newColorFilter(any<ColorFilter>()) }
        verify(exactly = 1) { viewModel.newColorFilter(any<ImageColorFilter>()) }
    }

    @Test
    fun testGrayScale() {
        colorFilterUseCase.grayScale()

        verify(exactly = 0) { viewModel.newColorFilter(any<ColorFilter>()) }
        verify(exactly = 1) { viewModel.newColorFilter(any<ImageColorFilter>()) }
    }

}