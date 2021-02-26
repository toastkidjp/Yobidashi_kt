/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.pdf

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class PdfImageFactoryTest {

    private lateinit var pdfImageFactory: PdfImageFactory

    @MockK
    private lateinit var page: PdfRenderer.Page

    @MockK
    private lateinit var bitmap: Bitmap

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic(Bitmap::class)
        every { Bitmap.createBitmap(any(), any(), any()) }.returns(bitmap)

        every { page.getWidth() }.returns(10)
        every { page.getHeight() }.returns(10)
        every { page.render(bitmap, null, null, any()) }.answers { Unit }
        every { page.close() }.answers { Unit }

        pdfImageFactory = PdfImageFactory()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        pdfImageFactory.invoke(page)

        verify(exactly = 1) { Bitmap.createBitmap(any(), any(), any()) }
        verify(exactly = 1) { page.getWidth() }
        verify(exactly = 1) { page.getHeight() }
        verify(exactly = 1) { page.render(bitmap, null, null, any()) }
        verify(exactly = 1) { page.close() }
    }

}