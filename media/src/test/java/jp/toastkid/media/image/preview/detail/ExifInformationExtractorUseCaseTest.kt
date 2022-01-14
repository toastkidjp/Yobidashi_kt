/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.media.image.preview.detail

import androidx.exifinterface.media.ExifInterface
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class ExifInformationExtractorUseCaseTest {

    private lateinit var exifInformationExtractorUseCase: ExifInformationExtractorUseCase

    @MockK
    private lateinit var exifInterface: ExifInterface

    @MockK
    private lateinit var stringBuilder: StringBuilder

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        exifInformationExtractorUseCase = ExifInformationExtractorUseCase(stringBuilder)

        every { stringBuilder.append(any<String>()) }.returns(stringBuilder)
        every { stringBuilder.toString() }.returns("test")
        every { stringBuilder.setLength(any()) }.answers { Unit }

        every { exifInterface.getAttribute(any()) }.returns("test")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        exifInformationExtractorUseCase.invoke(exifInterface)

        verify (atLeast = 1) { stringBuilder.append(any<String>()) }
        verify (atLeast = 1) { stringBuilder.toString() }
        verify (atLeast = 1) { stringBuilder.setLength(any()) }
        verify (atLeast = 1) { exifInterface.getAttribute(any()) }
    }

}