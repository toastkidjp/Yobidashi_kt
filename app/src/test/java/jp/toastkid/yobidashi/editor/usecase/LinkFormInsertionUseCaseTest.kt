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

    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var editable: Editable

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { editText.getText() }.returns(editable)
        coEvery { editText.getSelectionStart() }.returns(2)
        coEvery { editable.insert(any(), any()) }.returns(editable)
        coEvery { editable.toString() }.returns("Too long text is good.")
        every { editText.getContext() }.returns(context)
        every { context.getString(any()) }.returns("test")
        every { contentViewModel.snackWithAction(any(), any(), any()) }.returns(Unit)
        coEvery { linkTitleFetcherUseCase.invoke(any()) }.returns("title")

        mockkObject(Clipboard)
        every { Clipboard.getPrimary(any()) }.returns("https://www.yahoo.co.jp")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        linkFormInsertionUseCase.invoke()

        verify(atLeast = 1) { editText.getText() }
        coVerify(atLeast = 1) { editText.getSelectionStart() }
        coVerify(exactly = 1) { editable.insert(any(), any()) }
        coVerify(exactly = 1) { editable.toString() }
        verify(atLeast = 1) { editText.getContext() }
        verify(atLeast = 1) { context.getString(any()) }
        verify(exactly = 1) { contentViewModel.snackWithAction(any(), any(), any()) }
        verify(exactly = 1) { Clipboard.getPrimary(any()) }
        coVerify(exactly = 1) { linkTitleFetcherUseCase.invoke(any()) }
    }

}