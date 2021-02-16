/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.media.image.preview

import android.graphics.Bitmap
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class RotatedBitmapFactoryTest {

    @InjectMockKs
    private lateinit var rotatedBitmapFactory: RotatedBitmapFactory

    @MockK
    private lateinit var rotateMatrixFactory: RotateMatrixFactory

    @MockK
    private lateinit var bitmap: Bitmap

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { rotateMatrixFactory.invoke(any(), any(), any()) }.returns(mockk())
        every { bitmap.getWidth() }.returns(1)
        every { bitmap.getHeight() }.returns(1)

        mockkStatic(Bitmap::class)
        every { Bitmap.createBitmap(any(), 0, 0, any(), any(), any(), false) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun rotateLeft() {
        rotatedBitmapFactory.rotateLeft(bitmap)

        verify(atLeast = 1) { bitmap.getWidth() }
        verify(atLeast = 1) { bitmap.getHeight() }
        verify(exactly = 1) { rotateMatrixFactory.invoke(any(), any(), any())  }
        verify(exactly = 1) { Bitmap.createBitmap(any(), 0, 0, any(), any(), any(), false)  }
    }

    @Test
    fun testRotateRight() {
        rotatedBitmapFactory.rotateRight(bitmap)

        verify(atLeast = 1) { bitmap.getWidth() }
        verify(atLeast = 1) { bitmap.getHeight() }
        verify(exactly = 1) { rotateMatrixFactory.invoke(any(), any(), any())  }
        verify(exactly = 1) { Bitmap.createBitmap(any(), 0, 0, any(), any(), any(), false)  }
    }

    @Test
    fun testReverse() {
        rotatedBitmapFactory.reverse(bitmap)

        verify(atLeast = 1) { bitmap.getWidth() }
        verify(atLeast = 1) { bitmap.getHeight() }
        verify(exactly = 0) { rotateMatrixFactory.invoke(any(), any(), any())  }
        verify(exactly = 1) { Bitmap.createBitmap(any(), 0, 0, any(), any(), any(), false)  }
    }

}