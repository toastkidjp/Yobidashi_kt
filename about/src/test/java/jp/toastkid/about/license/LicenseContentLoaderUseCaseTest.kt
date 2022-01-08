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

    private val content = "I have a question."

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { assetManager.open(any()) }.returns(content.byteInputStream())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        val readContent = licenseContentLoaderUseCase.invoke()

        assertEquals(content, readContent)

        verify { assetManager.open(any()) }
    }
}