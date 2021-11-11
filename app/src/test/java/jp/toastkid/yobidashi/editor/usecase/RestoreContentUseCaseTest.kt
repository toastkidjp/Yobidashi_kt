/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.editor.usecase

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
import jp.toastkid.yobidashi.editor.ContentHolderService
import org.junit.After
import org.junit.Before
import org.junit.Test

class RestoreContentUseCaseTest {

    @InjectMockKs
    private lateinit var restoreContentUseCase: RestoreContentUseCase

    @MockK
    private lateinit var contentHolderService: ContentHolderService

    @MockK
    private lateinit var contentViewModel: ContentViewModel

    @MockK
    private lateinit var editorInput: EditText

    @MockK
    private lateinit var setContentText: (String) -> Unit

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { contentHolderService.isBlank() }.returns(false)
        every { contentViewModel.snackShort(any<Int>()) }.just(Runs)
        every { editorInput.selectionStart }.returns(10)
        every { contentHolderService.getContent() }.returns("To be, or not to be, that is the question.")
        every { setContentText.invoke(any()) }.just(Runs)
        every { editorInput.setSelection(any()) }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testHoldingContentIsEmptyCase() {
        every { contentHolderService.isBlank() }.returns(true)

        restoreContentUseCase.invoke()

        verify(exactly = 1) { contentHolderService.isBlank() }
        verify(exactly = 1) { contentViewModel.snackShort(any<Int>()) }
    }

    @Test
    fun testLackOfContentLengthCase() {
        every { contentHolderService.getContent() }.returns("question")

        restoreContentUseCase.invoke()

        verify(exactly = 1) { contentHolderService.isBlank() }
        verify(exactly = 0) { contentViewModel.snackShort(any<Int>()) }
        verify(exactly = 1) { setContentText.invoke(any()) }
        verify(exactly = 1) { editorInput.setSelection("question".length) }
    }

}