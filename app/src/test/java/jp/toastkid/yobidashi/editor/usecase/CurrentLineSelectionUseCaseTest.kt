/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.editor.usecase

import android.text.Editable
import android.text.Layout
import android.widget.EditText
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class CurrentLineSelectionUseCaseTest {

    @InjectMockKs
    private lateinit var currentLineSelectionUseCase: CurrentLineSelectionUseCase

    @MockK
    private lateinit var editText: EditText

    @MockK
    private lateinit var layout: Layout

    @MockK
    private lateinit var editable: Editable

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { editText.selectionStart }.returns(2)
        every { editText.layout }.returns(layout)
        every { editText.post(any()) }.returns(true)

        every { layout.getLineForOffset(any()) }.returns(10)
        every { layout.getLineStart(any()) }.returns(127)
        every { layout.getLineEnd(any()) }.returns(133)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        currentLineSelectionUseCase.invoke(editText)

        verify(exactly = 1) { editText.selectionStart }
        verify(atLeast = 1) { editText.layout }

        verify(exactly = 1) { layout.getLineForOffset(any()) }
        verify(exactly = 1) { layout.getLineStart(any()) }
        verify(exactly = 1) { layout.getLineEnd(any()) }

        verify(exactly = 1) { editText.post(any()) }
    }

    @Test
    fun test() {
        every { layout.getLineStart(any()) }.returns(-1)

        currentLineSelectionUseCase.invoke(editText)

        verify(exactly = 1) { editText.selectionStart }
        verify(atLeast = 1) { editText.layout }

        verify(exactly = 1) { layout.getLineForOffset(any()) }
        verify(exactly = 1) { layout.getLineStart(any()) }
        verify(exactly = 1) { layout.getLineEnd(any()) }

        verify(exactly = 0) { editText.post(any()) }
    }

}