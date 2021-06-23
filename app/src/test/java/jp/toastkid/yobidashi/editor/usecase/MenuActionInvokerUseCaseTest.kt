/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.editor.usecase

import android.text.Editable
import android.widget.EditText
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.editor.ListHeadAdder
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.speech.SpeechMaker
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class MenuActionInvokerUseCaseTest {

    @InjectMockKs
    private lateinit var menuActionInvokerUseCase: MenuActionInvokerUseCase

    @MockK
    private lateinit var editText: EditText

    @MockK
    private lateinit var speechMaker: SpeechMaker

    @MockK
    private lateinit var browserViewModel: BrowserViewModel

    @MockK
    private lateinit var listHeadAdder: ListHeadAdder

    @MockK
    private lateinit var editable: Editable

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { editText.context }.returns(mockk())
        every { editText.getText() }.returns(editable)
        every { editText.getSelectionStart() }.returns(1)
        every { editable.insert(any(), any()) }.returns(editable)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        assertFalse(menuActionInvokerUseCase.invoke(-1, ""))

        verify(exactly = 1) { editText.context }
    }

    @Test
    fun testInsertAsPlain() {
        mockkObject(Clipboard)
        every { Clipboard.getPrimary(any()) }.returns("clipped")

        val handled = menuActionInvokerUseCase.invoke(R.id.context_edit_insert_as_plain, "")

        assertTrue(handled)
        verify(exactly = 1) { Clipboard.getPrimary(any()) }
        verify(exactly = 1) { editText.getSelectionStart() }
        verify(exactly = 1) { editable.insert(any(), any()) }
    }

    @Test
    fun testInsertAsPlainWithPrimaryClipIsEmpty() {
        mockkObject(Clipboard)
        every { Clipboard.getPrimary(any()) }.returns("")

        val handled = menuActionInvokerUseCase.invoke(R.id.context_edit_insert_as_plain, "")

        assertTrue(handled)
        verify(exactly = 1) { Clipboard.getPrimary(any()) }
        verify(exactly = 0) { editText.getSelectionStart() }
        verify(exactly = 0) { editable.insert(any(), any()) }
    }

    @Test
    fun testPasteAsQuotation() {
        mockkConstructor(PasteAsQuotationUseCase::class)
        every { anyConstructed<PasteAsQuotationUseCase>().invoke() }.just(Runs)

        val handled = menuActionInvokerUseCase.invoke(R.id.context_edit_paste_as_quotation, "test")

        assertTrue(handled)
        verify(exactly = 0) { anyConstructed<PasteAsQuotationUseCase>().invoke() }
    }

    @Test
    fun test() {
        val handled = menuActionInvokerUseCase.invoke(R.id.context_edit_horizontal_rule, "test")

        assertTrue(handled)
        verify(exactly = 1) { editText.getText() }
        verify(exactly = 1) { editText.getSelectionStart() }
        verify(exactly = 1) { editable.insert(any(), any()) }
    }

}