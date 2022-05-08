/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.usecase

import android.content.Context
import android.text.Editable
import android.widget.EditText
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.ContentViewModel
import org.junit.After
import org.junit.Before
import org.junit.Test

class TextCountUseCaseTest {

    @InjectMockKs
    private lateinit var textCountUseCase: TextCountUseCase

    @MockK
    private lateinit var editText: EditText

    @MockK
    private lateinit var contentViewModel: ContentViewModel

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var editable: Editable

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { editText.text }.returns(editable)
        every { editText.context }.returns(context)
        every { editText.selectionStart }.returns(0)
        every { editText.selectionEnd }.returns(20)
        every { editable.substring(any(), any()) }.returns("Earn this, earn it.")
        every { context.getString(any(), any()) }.returns("count: 20")
        every { contentViewModel.snackShort(any<String>()) }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        textCountUseCase.invoke(editText, contentViewModel)

        verify(exactly = 1) { editText.text }
        verify(exactly = 1) { editText.context }
        verify(exactly = 1) { editText.selectionStart }
        verify(exactly = 1) { editText.selectionEnd }
        verify(exactly = 1) { editable.substring(any(), any()) }
        verify(exactly = 1) { context.getString(any(), any()) }
        verify(exactly = 1) { contentViewModel.snackShort(any<String>()) }
    }

}