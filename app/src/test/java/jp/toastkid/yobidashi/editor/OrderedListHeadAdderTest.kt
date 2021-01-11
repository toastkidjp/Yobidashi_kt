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
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class OrderedListHeadAdderTest {

    private lateinit var orderedListHeadAdder: OrderedListHeadAdder

    @Before
    fun setUp() {
        orderedListHeadAdder = OrderedListHeadAdder()
    }

    @Test
    fun testInvoke() {
        val text = mockk<Editable>()
        every { text.subSequence(any(), any()) }.returns("tomato")
        every { text.replace(any(), any(), any<String>()) }.returns(mockk())

        val editText = mockk<EditText>()
        every { editText.text }.returns(text)
        every { editText.selectionStart }.returns(2)
        every { editText.selectionEnd }.returns(3)

        orderedListHeadAdder.invoke(editText)

        verify(atLeast = 1) { text.replace(2, 3, any<String>()) }
    }
    
}