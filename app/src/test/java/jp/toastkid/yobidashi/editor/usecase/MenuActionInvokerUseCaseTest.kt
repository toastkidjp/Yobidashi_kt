/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.editor.usecase

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
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
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.search.UrlFactory
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.editor.CurrentLineDuplicatorUseCase
import jp.toastkid.yobidashi.editor.ListHeadAdder
import jp.toastkid.yobidashi.editor.OrderedListHeadAdder
import jp.toastkid.yobidashi.editor.StringSurroundingUseCase
import jp.toastkid.yobidashi.editor.TableConverter
import jp.toastkid.yobidashi.libs.Inputs
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

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var preferences: SharedPreferences

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { editText.context }.returns(context)
        every { editText.getText() }.returns(editable)
        every { editText.getSelectionStart() }.returns(1)
        every { editable.insert(any(), any()) }.returns(editable)
        every { speechMaker.invoke(any()) }.just(Runs)
        every { listHeadAdder.invoke(any(), any()) }.just(Runs)
        every { context.getSharedPreferences(any(), any()) }.returns(preferences)
        every { preferences.getString(any(), any()) }.returns("test")
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
    fun testHorizontalRule() {
        val handled = menuActionInvokerUseCase.invoke(R.id.context_edit_horizontal_rule, "test")

        assertTrue(handled)
        verify(exactly = 1) { editText.getText() }
        verify(exactly = 1) { editText.getSelectionStart() }
        verify(exactly = 1) { editable.insert(any(), any()) }
    }

    @Test
    fun testCurrentLineDuplication() {
        mockkConstructor(CurrentLineDuplicatorUseCase::class)
        every { anyConstructed<CurrentLineDuplicatorUseCase>().invoke(any()) }.just(Runs)

        val handled = menuActionInvokerUseCase.invoke(R.id.context_edit_duplicate_current_line, "test")

        assertTrue(handled)
        verify(exactly = 1) { anyConstructed<CurrentLineDuplicatorUseCase>().invoke(any()) }
    }

    @Test
    fun testOrderedList() {
        mockkConstructor(OrderedListHeadAdder::class)
        every { anyConstructed<OrderedListHeadAdder>().invoke(any()) }.just(Runs)

        val handled = menuActionInvokerUseCase.invoke(R.id.context_edit_add_order, "test")

        assertTrue(handled)
        verify(exactly = 1) { anyConstructed<OrderedListHeadAdder>().invoke(any()) }
    }

    @Test
    fun testTableConverter() {
        mockkConstructor(TableConverter::class)
        every { anyConstructed<TableConverter>().invoke(any()) }.just(Runs)

        val handled = menuActionInvokerUseCase.invoke(R.id.context_edit_convert_to_table, "test")

        assertTrue(handled)
        verify(exactly = 1) { anyConstructed<TableConverter>().invoke(any()) }
    }

    @Test
    fun testUnorderedList() {
        val handled = menuActionInvokerUseCase.invoke(R.id.context_edit_unordered_list, "test")

        assertTrue(handled)
        verify(exactly = 1) { listHeadAdder.invoke(any(), any()) }
    }

    @Test
    fun testTaskList() {
        val handled = menuActionInvokerUseCase.invoke(R.id.context_edit_task_list, "test")

        assertTrue(handled)
        verify(exactly = 1) { listHeadAdder.invoke(any(), any()) }
    }

    @Test
    fun testAddQuote() {
        val handled = menuActionInvokerUseCase.invoke(R.id.context_edit_add_quote, "test")

        assertTrue(handled)
        verify(exactly = 1) { listHeadAdder.invoke(any(), any()) }
    }

    @Test
    fun testDoubleQuote() {
        mockkConstructor(StringSurroundingUseCase::class)
        every { anyConstructed<StringSurroundingUseCase>().invoke(any(), any()) }.just(Runs)

        val handled = menuActionInvokerUseCase.invoke(R.id.context_edit_double_quote, "test")

        assertTrue(handled)
        verify(exactly = 1) { anyConstructed<StringSurroundingUseCase>().invoke(any(), any()) }
    }

    @Test
    fun testSpeech() {
        val handled = menuActionInvokerUseCase.invoke(R.id.context_edit_speech, "test")

        assertTrue(handled)
        verify(exactly = 1) { speechMaker.invoke(any()) }
    }

    @Test
    fun testSpeech2() {
        val handled = menuActionInvokerUseCase.invoke(R.id.context_edit_speech, " ")

        assertTrue(handled)
        verify(exactly = 1) { speechMaker.invoke(any()) }
        verify(exactly = 1) { editText.getText() }
    }

    @Test
    fun testOpenNew() {
        every { browserViewModel.open(any()) }.just(Runs)
        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.returns(mockk())

        val handled = menuActionInvokerUseCase
            .invoke(R.id.context_edit_url_open_new, "https://www.yahoo.co.jp")

        assertTrue(handled)
        verify(exactly = 1) { browserViewModel.open(any()) }
        verify(exactly = 1) { Uri.parse(any()) }
    }

    @Test
    fun testOpenBackground() {
        every { browserViewModel.openBackground(any()) }.just(Runs)
        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.returns(mockk())

        val handled = menuActionInvokerUseCase
            .invoke(R.id.context_edit_url_open_background, "https://www.yahoo.co.jp")

        assertTrue(handled)
        verify(exactly = 1) { browserViewModel.openBackground(any()) }
        verify(exactly = 1) { Uri.parse(any()) }
    }

    @Test
    fun testPreview() {
        every { browserViewModel.preview(any()) }.just(Runs)
        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.returns(mockk())
        mockkObject(Inputs)
        every { Inputs.hideKeyboard(any()) }.just(Runs)

        val handled = menuActionInvokerUseCase
            .invoke(R.id.context_edit_url_preview, "https://www.yahoo.co.jp")

        assertTrue(handled)
        verify(exactly = 1) { browserViewModel.preview(any()) }
        verify(exactly = 1) { Uri.parse(any()) }
        verify(exactly = 1) { Inputs.hideKeyboard(any()) }
    }

    @Test
    fun testWebSearch() {
        every { browserViewModel.open(any()) }.just(Runs)
        mockkConstructor(UrlFactory::class)
        every { anyConstructed<UrlFactory>().invoke(any(), any(), any()) }.returns(mockk())

        val handled = menuActionInvokerUseCase
            .invoke(R.id.context_edit_web_search, "https://www.yahoo.co.jp")

        assertTrue(handled)
        verify(exactly = 1) { browserViewModel.open(any()) }
        verify(exactly = 1) { anyConstructed<UrlFactory>().invoke(any(), any(), any()) }
    }

    @Test
    fun testPreviewSearch() {
        every { browserViewModel.preview(any()) }.just(Runs)
        mockkConstructor(UrlFactory::class)
        every { anyConstructed<UrlFactory>().invoke(any(), any(), any()) }.returns(mockk())

        val handled = menuActionInvokerUseCase
            .invoke(R.id.context_edit_preview_search, "https://www.yahoo.co.jp")

        assertTrue(handled)
        verify(exactly = 1) { browserViewModel.preview(any()) }
        verify(exactly = 1) { anyConstructed<UrlFactory>().invoke(any(), any(), any()) }
    }

}