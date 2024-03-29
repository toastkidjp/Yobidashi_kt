/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor

import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.yobidashi.libs.speech.SpeechMaker
import org.junit.After
import org.junit.Before
import org.junit.Test

class EditorContextMenuInitializerTest {

    @InjectMockKs
    private lateinit var editorContextMenuInitializer: EditorContextMenuInitializer

    @MockK
    private lateinit var editText: EditText

    @MockK
    private lateinit var speechMaker: SpeechMaker

    @MockK
    private lateinit var viewModelProvider: ViewModelProvider

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { editText.context }.returns(mockk())
        every { viewModelProvider.get(BrowserViewModel::class.java) }.returns(mockk())
        every { viewModelProvider.get(ContentViewModel::class.java) }.returns(mockk())

        every { editText.customInsertionActionModeCallback = any() }.just(Runs)
        every { editText.customSelectionActionModeCallback = any() }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testEditTextIsNullCase() {
        editorContextMenuInitializer.invoke(null, speechMaker, viewModelProvider)

        verify(exactly = 0) { editText.context }
    }

    @Test
    fun testCompleteInitializing() {
        editorContextMenuInitializer.invoke(editText, speechMaker, viewModelProvider)

        verify(exactly = 1) { editText.context }
        verify(exactly = 1) { editText.customInsertionActionModeCallback = any() }
        verify(exactly = 1) { editText.customSelectionActionModeCallback = any() }
    }

}