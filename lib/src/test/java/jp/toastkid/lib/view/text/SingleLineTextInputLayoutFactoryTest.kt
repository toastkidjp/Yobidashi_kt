/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.view.text

import android.view.ViewGroup
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class SingleLineTextInputLayoutFactoryTest {

    private lateinit var singleLineTextInputLayoutFactory: SingleLineTextInputLayoutFactory

    @MockK
    private lateinit var textInputLayout: TextInputLayout

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { textInputLayout.addView(any(), any(), any<ViewGroup.LayoutParams>()) }.returns(Unit)

        mockkConstructor(EditText::class)
        every { anyConstructed<EditText>().setMaxLines(any()) }.returns(Unit)
        every { anyConstructed<EditText>().setInputType(any()) }.returns(Unit)
        every { anyConstructed<EditText>().setImeOptions(any()) }.returns(Unit)

        singleLineTextInputLayoutFactory = SingleLineTextInputLayoutFactory({ textInputLayout })
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        singleLineTextInputLayoutFactory.invoke(mockk())

        verify(exactly = 1) { textInputLayout.addView(any(), any(), any<ViewGroup.LayoutParams>()) }
        verify(exactly = 1) { anyConstructed<EditText>().setMaxLines(any()) }
        verify(exactly = 1) { anyConstructed<EditText>().setInputType(any()) }
        verify(exactly = 1) { anyConstructed<EditText>().setImeOptions(any()) }
    }

}