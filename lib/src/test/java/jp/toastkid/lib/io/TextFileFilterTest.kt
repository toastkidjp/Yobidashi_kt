/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.io

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class TextFileFilterTest {

    @InjectMockKs
    private lateinit var textFileFilter: TextFileFilter

    @MockK
    private lateinit var file: File

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { file.name }.returns("test.txt")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testTextFileCase() {
        assertTrue(textFileFilter.accept(file))
    }

    @Test
    fun testAcceptMd() {
        every { file.name }.returns("test.md")

        assertTrue(textFileFilter.accept(file))
    }

}