/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.media.image.preview

import android.graphics.ColorFilter
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
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
        MockKAnnotations.init(this)

        every { viewModel.newColorFilter(any<ColorFilter>()) }.answers { Unit }
        every { viewModel.newColorFilter(any<ImageColorFilter>()) }.answers { Unit }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun applyAlpha() {
        colorFilterUseCase.applyAlpha(0.5f)

        verify(exactly = 1) { viewModel.newColorFilter(any<ColorFilter>()) }
        verify(exactly = 0) { viewModel.newColorFilter(any<ImageColorFilter>()) }
    }

    @Test
    fun applyContrast() {
        colorFilterUseCase.applyContrast(0.5f)

        verify(exactly = 1) { viewModel.newColorFilter(any<ColorFilter>()) }
        verify(exactly = 0) { viewModel.newColorFilter(any<ImageColorFilter>()) }
    }

    @Test
    fun reverseFilter() {
        colorFilterUseCase.reverseFilter()

        verify(exactly = 0) { viewModel.newColorFilter(any<ColorFilter>()) }
        verify(exactly = 1) { viewModel.newColorFilter(any<ImageColorFilter>()) }
    }

    @Test
    fun sepia() {
        colorFilterUseCase.sepia()

        verify(exactly = 0) { viewModel.newColorFilter(any<ColorFilter>()) }
        verify(exactly = 1) { viewModel.newColorFilter(any<ImageColorFilter>()) }
    }

    @Test
    fun grayScale() {
        colorFilterUseCase.grayScale()

        verify(exactly = 0) { viewModel.newColorFilter(any<ColorFilter>()) }
        verify(exactly = 1) { viewModel.newColorFilter(any<ImageColorFilter>()) }
    }

}