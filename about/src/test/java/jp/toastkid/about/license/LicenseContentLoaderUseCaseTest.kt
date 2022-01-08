/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.about.license

import android.content.res.AssetManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test

class LicenseContentLoaderUseCaseTest {

    @InjectMockKs
    private lateinit var licenseContentLoaderUseCase: LicenseContentLoaderUseCase

    @MockK
    private lateinit var assetManager: AssetManager

    private val singleLineContent = "I have a question."

    private val multiLineContent =
        arrayOf("I have a question.", "Did you have any questions?", "Thanks.")
            .joinToString(System.lineSeparator())

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { assetManager.open(any()) }.returns(singleLineContent.byteInputStream())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testSingleLineTextCase() {
        val readContent = licenseContentLoaderUseCase.invoke()

        assertEquals(singleLineContent, readContent)

        verify { assetManager.open(any()) }
    }

    @Test
    fun test() {
        every { assetManager.open(any()) }.returns(multiLineContent.byteInputStream())

        val readContent = licenseContentLoaderUseCase.invoke()

        assertEquals(multiLineContent, readContent)

        verify { assetManager.open(any()) }
    }
}