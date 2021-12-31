/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.editor.usecase

import android.widget.EditText
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test

class ThousandSeparatorInsertionUseCaseTest {

    @InjectMockKs
    private lateinit var thousandSeparatorInsertionUseCase: ThousandSeparatorInsertionUseCase

    @MockK
    private lateinit var editText: EditText

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { editText.text.replace(any(), any(), any()) }.returns(mockk())
        every { editText.selectionStart }.returns(0)
        every { editText.selectionEnd }.returns(4)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testIgnvoke() {
        thousandSeparatorInsertionUseCase.invoke(editText, "1231")

        every { editText.text.replace(any(), any(), any()) }
    }
}