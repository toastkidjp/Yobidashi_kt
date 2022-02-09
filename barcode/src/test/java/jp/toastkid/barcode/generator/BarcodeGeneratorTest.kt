/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.barcode.generator

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class BarcodeGeneratorTest {

    @InjectMockKs
    private lateinit var barcodeGenerator: BarcodeGenerator

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkConstructor(BarcodeEncoder::class)
        every { anyConstructed<BarcodeEncoder>().encodeBitmap(any(), any(), any(), any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testCorrectCase() {
        barcodeGenerator.invoke("https://www.yahoo.co.jp", 400)

        verify { anyConstructed<BarcodeEncoder>().encodeBitmap(any(), BarcodeFormat.QR_CODE, any(), any()) }
    }

    @Test
    fun testPassedIllegalSize() {
        mockkStatic(Bitmap::class)
        every { Bitmap.createBitmap(any(), any(), any()) }.returns(mockk())

        barcodeGenerator.invoke("https://www.yahoo.co.jp", -1)

        verify(inverse = true) { anyConstructed<BarcodeEncoder>().encodeBitmap(any(), BarcodeFormat.QR_CODE, any(), any()) }
    }
}