/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.barcode.model

import android.media.Image
import androidx.camera.core.ImageProxy
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify

class BarcodeAnalyzerTest {

    @InjectMockKs
    private lateinit var subject: BarcodeAnalyzer

    @MockK
    private lateinit var callback: (String) -> Unit

    @MockK
    private lateinit var imageProxy: ImageProxy

    @MockK
    private lateinit var image: Image

    @org.junit.Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { imageProxy.image } returns image
        every { imageProxy.format } returns 1
        every { imageProxy.close() } just Runs
        every { callback.invoke(any()) } just Runs

        mockkConstructor(BarcodeAnalyzerImplementation::class)
        every { anyConstructed<BarcodeAnalyzerImplementation>().invoke(any(), any()) } returns "test"
    }

    @org.junit.After
    fun tearDown() {
        unmockkAll()
    }

    @org.junit.Test
    fun analyze() {
        subject.analyze(imageProxy)

        verify { callback.invoke("test") }
        verify { imageProxy.close() }
    }

    @org.junit.Test
    fun returnsNull() {
        every { anyConstructed<BarcodeAnalyzerImplementation>().invoke(any(), any()) } returns null

        subject.analyze(imageProxy)

        verify(inverse = true) { callback.invoke(any()) }
        verify { imageProxy.close() }
    }

}