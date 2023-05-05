/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.translate

import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TranslationUrlGeneratorTest {

    @InjectMockKs
    private lateinit var translationUrlGenerator: TranslationUrlGenerator

    private val expectedE2J = "sl=en&tl=ja"

    private val expectedJ2E = "sl=ja&tl=en"

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic(Uri::class)
        every { Uri.encode(any()) }.returns("Mocked")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testEmptyCase() {
        assertTrue(translationUrlGenerator("").contains(expectedE2J))
    }

    @Test
    fun testOnlySingleByteCharacters() {
        assertTrue(translationUrlGenerator("Make sense?").contains(expectedE2J))
    }

    @Test
    fun testContainsMultiByteCharacters() {
        assertTrue(translationUrlGenerator("きょうは Chicken Noodle だ。").contains(expectedJ2E))
    }

}