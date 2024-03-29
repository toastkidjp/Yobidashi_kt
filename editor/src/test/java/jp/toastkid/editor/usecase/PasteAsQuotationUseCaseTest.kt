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
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.clip.Clipboard
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class PasteAsQuotationUseCaseTest {

    @InjectMockKs
    private lateinit var pasteAsQuotationUseCase: PasteAsQuotationUseCase

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
        every { editText.getSelectionStart() }.returns(2)
        every { editable.insert(any(), any()) }.returns(editable)
        every { editable.toString() }.returns("Too long text is good.")
        every { editText.getContext() }.returns(context)
        every { context.getString(any()) }.returns("test")
        every { contentViewModel.snackWithAction(any(), any(), any()) }.returns(Unit)

        mockkObject(Clipboard)
        every { Clipboard.getPrimary(any()) }.returns("test-primary")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        pasteAsQuotationUseCase.invoke()

        verify(atLeast = 1) { editText.getText() }
        verify(atLeast = 1) { editText.getSelectionStart() }
        verify(exactly = 1) { editable.insert(any(), any()) }
        verify(exactly = 1) { editable.toString() }
        verify(atLeast = 1) { editText.getContext() }
        verify(atLeast = 1) { context.getString(any()) }
        verify(exactly = 1) { contentViewModel.snackWithAction(any(), any(), any()) }
        verify(exactly = 1) { Clipboard.getPrimary(any()) }
    }

    @Test
    fun testPrimaryClipIsNull() {
        every { Clipboard.getPrimary(any()) }.returns(null)

        pasteAsQuotationUseCase.invoke()

        verify(exactly = 0) { editText.getText() }
        verify(exactly = 0) { editText.getSelectionStart() }
        verify(exactly = 0) { editable.insert(any(), any()) }
        verify(exactly = 0) { editable.toString() }
        verify(atLeast = 1) { editText.getContext() }
        verify(exactly = 0) { context.getString(any()) }
        verify(exactly = 0) { contentViewModel.snackWithAction(any(), any(), any()) }
        verify(exactly = 1) { Clipboard.getPrimary(any()) }
    }

    @Test
    fun testPrimaryClipIsEmpty() {
        every { Clipboard.getPrimary(any()) }.returns("")

        pasteAsQuotationUseCase.invoke()

        verify(exactly = 0) { editText.getText() }
        verify(exactly = 0) { editText.getSelectionStart() }
        verify(exactly = 0) { editable.insert(any(), any()) }
        verify(exactly = 0) { editable.toString() }
        verify(atLeast = 1) { editText.getContext() }
        verify(exactly = 0) { context.getString(any()) }
        verify(exactly = 0) { contentViewModel.snackWithAction(any(), any(), any()) }
        verify(exactly = 1) { Clipboard.getPrimary(any()) }
    }

}