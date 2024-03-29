/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.usecase

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
import jp.toastkid.editor.CurrentLineDuplicatorUseCase
import jp.toastkid.editor.ListHeadAdder
import jp.toastkid.editor.OrderedListHeadAdder
import jp.toastkid.editor.R
import jp.toastkid.editor.StringSurroundingUseCase
import jp.toastkid.editor.TableConverter
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.clip.Clipboard
import jp.toastkid.lib.input.Inputs
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.translate.TranslationUrlGenerator
import jp.toastkid.libs.speech.SpeechMaker
import jp.toastkid.search.SearchCategory
import jp.toastkid.search.UrlFactory
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
    private lateinit var contentViewModel: ContentViewModel

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
        verify(exactly = 1) { anyConstructed<PasteAsQuotationUseCase>().invoke() }
    }

    @Test
    fun testPasteUrlWithTitle() {
        mockkConstructor(LinkFormInsertionUseCase::class)
        every { anyConstructed<LinkFormInsertionUseCase>().invoke() }.just(Runs)

        val handled = menuActionInvokerUseCase.invoke(R.id.context_edit_paste_url_with_title, "https://www.yahoo.co.jp")

        assertTrue(handled)
        verify(exactly = 1) { anyConstructed<LinkFormInsertionUseCase>().invoke() }
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
        verify(exactly = 0) { editText.getText() }
    }

    @Test
    fun testSpeechWithBlank() {
        val handled = menuActionInvokerUseCase.invoke(R.id.context_edit_speech, " ")

        assertTrue(handled)
        verify(exactly = 1) { speechMaker.invoke(any()) }
        verify(exactly = 1) { editText.getText() }
    }

    @Test
    fun testOpenNew() {
        every { contentViewModel.open(any()) }.just(Runs)
        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.returns(mockk())

        mockkConstructor(Inputs::class)
        every { anyConstructed<Inputs>().hideKeyboard(any()) }.just(Runs)

        val handled = menuActionInvokerUseCase
            .invoke(R.id.context_edit_url_open_new, "https://www.yahoo.co.jp")

        assertTrue(handled)
        verify(exactly = 1) { contentViewModel.open(any()) }
        verify(exactly = 1) { Uri.parse(any()) }
        verify { anyConstructed<Inputs>().hideKeyboard(any()) }
    }

    @Test
    fun testOpenBackground() {
        every { contentViewModel.openBackground(any()) }.just(Runs)
        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.returns(mockk())

        val handled = menuActionInvokerUseCase
            .invoke(R.id.context_edit_url_open_background, "https://www.yahoo.co.jp")

        assertTrue(handled)
        verify(exactly = 1) { contentViewModel.openBackground(any()) }
        verify(exactly = 1) { Uri.parse(any()) }
    }

    @Test
    fun testPreview() {
        every { contentViewModel.preview(any()) }.just(Runs)
        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.returns(mockk())

        val handled = menuActionInvokerUseCase
            .invoke(R.id.context_edit_url_preview, "https://www.yahoo.co.jp")

        assertTrue(handled)
        verify(exactly = 1) { contentViewModel.preview(any()) }
        verify(exactly = 1) { Uri.parse(any()) }
    }

    @Test
    fun testWebSearch() {
        every { contentViewModel.open(any()) }.just(Runs)
        mockkConstructor(UrlFactory::class)
        every { anyConstructed<UrlFactory>().invoke(any(), any(), any()) }.returns(mockk())

        mockkConstructor(Inputs::class)
        every { anyConstructed<Inputs>().hideKeyboard(any()) }.just(Runs)

        val handled = menuActionInvokerUseCase
            .invoke(R.id.context_edit_web_search, "https://www.yahoo.co.jp")

        assertTrue(handled)
        verify(exactly = 1) { contentViewModel.open(any()) }
        verify(exactly = 1) { anyConstructed<UrlFactory>().invoke(any(), any(), any()) }
        verify { anyConstructed<Inputs>().hideKeyboard(any()) }
    }

    @Test
    fun testPreviewSearch() {
        every { contentViewModel.preview(any()) }.just(Runs)
        mockkConstructor(UrlFactory::class)
        every { anyConstructed<UrlFactory>().invoke(any(), any(), any()) }.returns(mockk())

        val handled = menuActionInvokerUseCase
            .invoke(R.id.context_edit_preview_search, "https://www.yahoo.co.jp")

        assertTrue(handled)
        verify(exactly = 1) { contentViewModel.preview(any()) }
        verify(exactly = 1) { anyConstructed<UrlFactory>().invoke(any(), any(), any()) }
    }

    @Test
    fun testPreviewSearchWithPreferencesReturnsNull() {
        every { contentViewModel.preview(any()) }.just(Runs)
        mockkConstructor(UrlFactory::class)
        every { anyConstructed<UrlFactory>().invoke(any(), any(), any()) }.returns(mockk())
        mockkConstructor(PreferenceApplier::class)
        every { anyConstructed<PreferenceApplier>().getDefaultSearchEngine() }.returns(null)
        val mockUri = mockk<Uri>()
        every { mockUri.host }.returns("search.yahoo.co.jp")
        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.returns(mockUri)
        mockkObject(SearchCategory)
        every { SearchCategory.getDefaultCategoryName() }.returns("test")

        val handled = menuActionInvokerUseCase
            .invoke(R.id.context_edit_preview_search, "https://www.yahoo.co.jp")

        assertTrue(handled)
        verify(exactly = 1) { contentViewModel.preview(any()) }
        verify(exactly = 1) { anyConstructed<UrlFactory>().invoke(any(), any(), any()) }
        verify(exactly = 1) { anyConstructed<PreferenceApplier>().getDefaultSearchEngine() }
        verify(exactly = 1) { SearchCategory.getDefaultCategoryName() }
    }

    @Test
    fun testDeleteLine() {
        mockkConstructor(CurrentLineDeletionUseCase::class)
        every { anyConstructed<CurrentLineDeletionUseCase>().invoke(any()) }.just(Runs)

        val handled = menuActionInvokerUseCase
            .invoke(R.id.context_edit_delete_line, "")

        assertTrue(handled)
        verify(exactly = 1) { anyConstructed<CurrentLineDeletionUseCase>().invoke(any()) }
    }

    @Test
    fun testCount() {
        mockkConstructor(TextCountUseCase::class)
        every { anyConstructed<TextCountUseCase>().invoke(any(), any()) }.just(Runs)

        val handled = menuActionInvokerUseCase
            .invoke(R.id.context_edit_count, "test")

        assertTrue(handled)
        verify(exactly = 1) { anyConstructed<TextCountUseCase>().invoke(any(), any()) }
    }

    @Test
    fun testBold() {
        mockkConstructor(StringSurroundingUseCase::class)
        every { anyConstructed<StringSurroundingUseCase>().invoke(any(), any()) }.just(Runs)

        val handled = menuActionInvokerUseCase
            .invoke(R.id.context_edit_bold, "test")

        assertTrue(handled)
        verify(exactly = 1) { anyConstructed<StringSurroundingUseCase>().invoke(any(), any()) }
    }

    @Test
    fun testItalic() {
        mockkConstructor(StringSurroundingUseCase::class)
        every { anyConstructed<StringSurroundingUseCase>().invoke(any(), any()) }.just(Runs)

        val handled = menuActionInvokerUseCase
            .invoke(R.id.context_edit_italic, "test")

        assertTrue(handled)
        verify(exactly = 1) { anyConstructed<StringSurroundingUseCase>().invoke(any(), any()) }
    }

    @Test
    fun testStringSurrounding() {
        mockkConstructor(StringSurroundingUseCase::class)
        every { anyConstructed<StringSurroundingUseCase>().invoke(any(), any()) }.just(Runs)

        val handled = menuActionInvokerUseCase
            .invoke(R.id.context_edit_strikethrough, "test")

        assertTrue(handled)
        verify(exactly = 1) { anyConstructed<StringSurroundingUseCase>().invoke(any(), any()) }
    }

    @Test
    fun testTranslate() {
        mockkConstructor(TranslationUrlGenerator::class)
        every { anyConstructed<TranslationUrlGenerator>().invoke(any()) }.returns("https://test/url")
        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.returns(mockk())
        every { contentViewModel.preview(any()) }.just(Runs)

        val handled = menuActionInvokerUseCase
            .invoke(R.id.context_edit_translate, "test")

        assertTrue(handled)
        verify(exactly = 1) { anyConstructed<TranslationUrlGenerator>().invoke(any()) }
        verify { contentViewModel.preview(any()) }
    }

}