/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.editor

import android.text.Editable
import android.widget.EditText
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class TableConverterTest {

    private lateinit var tableConverter: TableConverter

    @MockK
    private lateinit var editText: EditText

    @MockK
    private lateinit var text: Editable

    @Before
    fun setUp() {
        tableConverter = TableConverter()

        MockKAnnotations.init(this)
    }

    @Test
    fun test() {
        every { text.subSequence(any(), any()) }.returns(
"""
key value
key2 value2
key3 0
""".trimIndent()
        )
        every { text.replace(any(), any(), any<String>()) }.returns(mockk())
        every { editText.text }.returns(text)
        every { editText.selectionStart }.returns(0)
        every { editText.selectionEnd }.returns(20)

        tableConverter.invoke(editText)

        verify(atLeast = 1) { text.replace(any(), any(), any<String>()) }
        verify(atLeast = 1) { editText.text }
        verify(atLeast = 1) { editText.selectionStart }
        verify(atLeast = 1) { editText.selectionEnd }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}