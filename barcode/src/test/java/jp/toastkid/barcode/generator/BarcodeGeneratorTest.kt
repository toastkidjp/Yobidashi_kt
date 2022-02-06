/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.barcode.generator

import com.journeyapps.barcodescanner.BarcodeEncoder
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify

class BarcodeGeneratorTest {

    @InjectMockKs
    private lateinit var barcodeGenerator: BarcodeGenerator

    @MockK
    private lateinit var barcodeEncoder : BarcodeEncoder

    @org.junit.Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { barcodeEncoder.encodeBitmap(any(), any(), any(), any()) }.returns(mockk())
    }

    @org.junit.After
    fun tearDown() {
        unmockkAll()
    }

    @org.junit.Test
    fun invoke() {
        barcodeGenerator.invoke("https://www.yahoo.co.jp", 400)

        verify { barcodeEncoder.encodeBitmap(any(), any(), any(), any()) }
    }
}