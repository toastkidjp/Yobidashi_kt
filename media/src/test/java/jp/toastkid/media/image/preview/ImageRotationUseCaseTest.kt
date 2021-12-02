/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.media.image.preview

import android.graphics.Bitmap
import android.graphics.Matrix
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class ImageRotationUseCaseTest {

    @InjectMockKs
    private lateinit var imageRotationUseCase: ImageRotationUseCase

    @MockK
    private lateinit var viewModel: ImagePreviewFragmentViewModel

    @MockK
    private lateinit var currentBitmap: () -> Bitmap

    @MockK
    private lateinit var rotatedBitmapFactory: RotatedBitmapFactory

    @Before
    fun setUp() {
        mockkConstructor(Matrix::class)
        every { anyConstructed<Matrix>().preScale(any(), any()) }.returns(true)

        MockKAnnotations.init(this)

        every { currentBitmap.invoke() }.returns(mockk())
        every { rotatedBitmapFactory.rotateLeft(any()) }.returns(mockk())
        every { rotatedBitmapFactory.rotateRight(any()) }.returns(mockk())
        every { rotatedBitmapFactory.reverse(any()) }.returns(mockk())
        every { viewModel.nextBitmap(any()) }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testRotateLeft() {
        imageRotationUseCase.rotateLeft()

        verify(exactly = 1) { currentBitmap.invoke() }
        verify(exactly = 1) { rotatedBitmapFactory.rotateLeft(any()) }
        verify(exactly = 0) { rotatedBitmapFactory.rotateRight(any()) }
        verify(exactly = 0) { rotatedBitmapFactory.reverse(any()) }
        verify(exactly = 1) { viewModel.nextBitmap(any()) }
    }

    @Test
    fun testRotateRight() {
        imageRotationUseCase.rotateRight()

        verify(exactly = 1) { currentBitmap.invoke() }
        verify(exactly = 0) { rotatedBitmapFactory.rotateLeft(any()) }
        verify(exactly = 1) { rotatedBitmapFactory.rotateRight(any()) }
        verify(exactly = 0) { rotatedBitmapFactory.reverse(any()) }
        verify(exactly = 1) { viewModel.nextBitmap(any()) }
    }

    @Test
    fun testReverse() {
        imageRotationUseCase.reverse()

        verify(exactly = 1) { currentBitmap.invoke() }
        verify(exactly = 0) { rotatedBitmapFactory.rotateLeft(any()) }
        verify(exactly = 0) { rotatedBitmapFactory.rotateRight(any()) }
        verify(exactly = 1) { rotatedBitmapFactory.reverse(any()) }
        verify(exactly = 1) { viewModel.nextBitmap(any()) }
    }

}