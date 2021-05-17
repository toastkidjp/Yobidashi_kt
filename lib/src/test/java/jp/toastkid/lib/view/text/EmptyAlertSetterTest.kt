/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.view.text

import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class EmptyAlertSetterTest {

    private lateinit var emptyAlertSetter: EmptyAlertSetter

    @MockK
    private lateinit var inputLayout: TextInputLayout

    @MockK
    private lateinit var editText: EditText

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { inputLayout.getEditText() }.returns(editText)
        every { inputLayout.getContext() }.returns(mockk())
        every { editText.addTextChangedListener(any()) }.returns(Unit)

        emptyAlertSetter = EmptyAlertSetter()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        emptyAlertSetter.invoke(inputLayout)

        verify(exactly = 1) { inputLayout.getEditText() }
        verify(exactly = 1) { editText.addTextChangedListener(any()) }
    }

    @Test
    fun testNullCase() {
        every { inputLayout.getEditText() }.returns(null)

        emptyAlertSetter.invoke(inputLayout)

        verify(exactly = 1) { inputLayout.getEditText() }
        verify(exactly = 1) { inputLayout.getContext() }
        verify(exactly = 0) { editText.addTextChangedListener(any()) }
    }

}