/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.editor.usecase

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
        every { editText.getText() }.returns(editable)
        every { editText.getContext() }.returns(context)
        every { editText.getSelectionStart() }.returns(0)
        every { editText.getSelectionEnd() }.returns(20)
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

        verify(exactly = 1) { editText.getText() }
        verify(exactly = 1) { editText.getContext() }
        verify(exactly = 1) { editText.getSelectionStart() }
        verify(exactly = 1) { editText.getSelectionEnd() }
        verify(exactly = 1) { editable.substring(any(), any()) }
        verify(exactly = 1) { context.getString(any(), any()) }
        verify(exactly = 1) { contentViewModel.snackShort(any<String>()) }
    }
}