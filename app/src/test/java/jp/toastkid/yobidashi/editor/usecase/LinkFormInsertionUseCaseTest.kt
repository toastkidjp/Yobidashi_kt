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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.yobidashi.libs.clip.Clipboard
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test

class LinkFormInsertionUseCaseTest {

    @InjectMockKs
    private lateinit var linkFormInsertionUseCase: LinkFormInsertionUseCase

    @MockK
    private lateinit var editText: EditText

    @MockK
    private lateinit var contentViewModel: ContentViewModel

    @MockK
    private lateinit var linkTitleFetcherUseCase: LinkTitleFetcherUseCase

    @Suppress("unused")
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @Suppress("unused")
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var editable: Editable

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { editText.text }.returns(editable)
        coEvery { editText.selectionStart }.returns(2)
        coEvery { editable.insert(any(), any()) }.returns(editable)
        coEvery { editable.toString() }.returns("Too long text is good.")
        every { editText.context }.returns(context)
        every { context.getString(any()) }.returns("test")
        coEvery { contentViewModel.snackWithAction(any(), any(), any()) }.returns(Unit)
        coEvery { linkTitleFetcherUseCase.invoke(any()) }.returns("title")

        mockkObject(Urls)
        every { Urls.isInvalidUrl(any()) }.returns(false)

        mockkObject(Clipboard)
        every { Clipboard.getPrimary(any()) }.returns("https://www.yahoo.co.jp")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testSuccessfulCase() {
        linkFormInsertionUseCase.invoke()

        verify(atLeast = 1) { editText.text }
        coVerify(atLeast = 1) { editText.selectionStart }
        coVerify(exactly = 1) { editable.insert(any(), any()) }
        coVerify(exactly = 1) { editable.toString() }
        verify(atLeast = 1) { Urls.isInvalidUrl(any()) }
        verify(atLeast = 1) { editText.context }
        verify(atLeast = 1) { context.getString(any()) }
        coVerify(exactly = 1) { contentViewModel.snackWithAction(any(), any(), any()) }
        verify(exactly = 1) { Clipboard.getPrimary(any()) }
        coVerify(exactly = 1) { linkTitleFetcherUseCase.invoke(any()) }
    }

    @Test
    fun testClipIsNullCase() {
        every { Clipboard.getPrimary(any()) }.returns(null)

        linkFormInsertionUseCase.invoke()

        verify(exactly = 0) { editText.getText() }
        coVerify(exactly = 0) { editText.getSelectionStart() }
        coVerify(exactly = 0) { editable.insert(any(), any()) }
        coVerify(exactly = 0) { editable.toString() }
        verify(exactly = 0) { Urls.isInvalidUrl(any()) }
        verify(atLeast = 1) { editText.getContext() }
        verify(exactly = 0) { context.getString(any()) }
        verify(exactly = 0) { contentViewModel.snackWithAction(any(), any(), any()) }
        verify(exactly = 1) { Clipboard.getPrimary(any()) }
        coVerify(exactly = 0) { linkTitleFetcherUseCase.invoke(any()) }
    }

    @Test
    fun testClipIsEmptyCase() {
        every { Clipboard.getPrimary(any()) }.returns("")

        linkFormInsertionUseCase.invoke()

        verify(exactly = 0) { editText.getText() }
        coVerify(exactly = 0) { editText.getSelectionStart() }
        coVerify(exactly = 0) { editable.insert(any(), any()) }
        coVerify(exactly = 0) { editable.toString() }
        verify(exactly = 0) { Urls.isInvalidUrl(any()) }
        verify(atLeast = 1) { editText.getContext() }
        verify(exactly = 0) { context.getString(any()) }
        verify(exactly = 0) { contentViewModel.snackWithAction(any(), any(), any()) }
        verify(exactly = 1) { Clipboard.getPrimary(any()) }
        coVerify(exactly = 0) { linkTitleFetcherUseCase.invoke(any()) }
    }

}