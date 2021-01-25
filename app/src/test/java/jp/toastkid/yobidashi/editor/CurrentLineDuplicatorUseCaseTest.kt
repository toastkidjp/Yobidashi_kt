/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.editor

import android.text.Editable
import android.text.Layout
import android.widget.EditText
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class CurrentLineDuplicatorUseCaseTest {

    private lateinit var currentLineDuplicatorUseCase: CurrentLineDuplicatorUseCase

    @MockK
    private lateinit var editText: EditText

    @MockK
    private lateinit var layout: Layout

    @MockK
    private lateinit var text: Editable

    @Before
    fun setUp() {
        currentLineDuplicatorUseCase = CurrentLineDuplicatorUseCase()

        MockKAnnotations.init(this)

        every { editText.getSelectionStart() }.returns(2)
        every { editText.layout }.returns(layout)
        every { editText.text }.returns(text)

        every { layout.getLineForOffset(any()) }.returns(10)
        every { layout.getLineStart(any()) }.returns(127)
        every { layout.getLineEnd(any()) }.returns(133)

        every { text.insert(any(), any()) }.answers { text }
        every { text.substring(any(), any()) }.returns("test")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        currentLineDuplicatorUseCase.invoke(editText)

        verify (atLeast = 1) { editText.getSelectionStart() }
        verify (atLeast = 1) { editText.layout }
        verify (atLeast = 1) { editText.text }
        verify (atLeast = 1) { layout.getLineForOffset(any()) }
        verify (atLeast = 1) { layout.getLineStart(any()) }
        verify (atLeast = 1) { layout.getLineEnd(any()) }
        verify (atLeast = 1) { text.insert(any(), any()) }
        verify (atLeast = 1) { text.substring(any(), any()) }
    }

    @Test
    fun testEarlyReturnCase() {
        every { layout.getLineStart(any()) }.returns(-1)
        every { layout.getLineEnd(any()) }.returns(133)

        currentLineDuplicatorUseCase.invoke(editText)

        verify (atLeast = 1) { editText.getSelectionStart() }
        verify (atLeast = 1) { editText.layout }
        verify (exactly = 0) { editText.text }
        verify (atLeast = 1) { layout.getLineForOffset(any()) }
        verify (atLeast = 1) { layout.getLineStart(any()) }
        verify (atLeast = 1) { layout.getLineEnd(any()) }
        verify (exactly = 0) { text.insert(any(), any()) }
        verify (exactly = 0) { text.substring(any(), any()) }
    }

}