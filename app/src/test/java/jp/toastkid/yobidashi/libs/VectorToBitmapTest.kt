/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.libs

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class VectorToBitmapTest {

    @InjectMockKs
    private lateinit var vectorToBitmap: VectorToBitmap

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var drawable: Drawable

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { drawable.setBounds(any(), any(), any(), any()) }.answers { Unit }
        every { drawable.draw(any()) }.answers { Unit }
        every { drawable.getIntrinsicWidth() }.returns(1)
        every { drawable.getIntrinsicHeight() }.returns(1)

        mockkConstructor(Canvas::class)
        every { anyConstructed<Canvas>().getWidth() }.returns(1)
        every { anyConstructed<Canvas>().getHeight() }.returns(1)

        mockkStatic(Bitmap::class)
        every { Bitmap.createBitmap(any(), any(), any()) }.returns(mockk())

        mockkStatic(ContextCompat::class)
        every { ContextCompat.getDrawable(any(), any())  }.returns(drawable)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        vectorToBitmap.invoke(1)

        verify(exactly = 1) { drawable.setBounds(any(), any(), any(), any()) }
        verify(exactly = 1) { drawable.draw(any()) }
        verify(exactly = 1) { drawable.getIntrinsicWidth() }
        verify(exactly = 1) { drawable.getIntrinsicHeight() }
        verify(exactly = 1) { anyConstructed<Canvas>().getWidth() }
        verify(exactly = 1) { anyConstructed<Canvas>().getHeight() }
        verify(exactly = 1) { Bitmap.createBitmap(any(), any(), any()) }
        verify(exactly = 1) { ContextCompat.getDrawable(any(), any())  }
    }
}