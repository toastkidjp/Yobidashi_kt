/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.view

import android.widget.EditText
import androidx.compose.foundation.text.input.clearText
import androidx.compose.ui.text.TextRange
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class EditorTabViewModelTest {

    /**
     * [EditText]
     */
    private lateinit var viewModel: EditorTabViewModel

    @Before
    fun setUp() {
        viewModel = EditorTabViewModel()
    }

    @org.junit.Test
    fun content() {
    }

    @org.junit.Test
    fun onValueChange() {
    }

    @org.junit.Test
    fun setMultiParagraph() {
    }

    @org.junit.Test
    fun getLineHeight() {
    }

    @org.junit.Test
    fun lineNumberScrollState() {
    }

    @org.junit.Test
    fun onClickLineNumber() {
    }

    @org.junit.Test
    fun focusRequester() {
    }

    @org.junit.Test
    fun initialScroll() {
    }

    @org.junit.Test
    fun lineNumbers() {
    }

    @org.junit.Test
    fun launchTab() {
    }

    @org.junit.Test
    fun currentLineOffset() {
    }

    @org.junit.Test
    fun currentLineHighlightColor() {
    }

    @org.junit.Test
    fun visualTransformation() {
    }

    @org.junit.Test
    fun makeCharacterCountMessage() {
    }

    @org.junit.Test
    fun dispose() {
    }

    @org.junit.Test
    fun insertText() {
    }

    @org.junit.Test
    fun replaceText() {
    }

    @Test
    fun findUp() {
        viewModel.content().clearText()
        viewModel.content().edit {
            append("abc is abc, you don't find abcd.")
            selection = TextRange(31)
        }

        viewModel.findUp("abc")
        assertEquals(27, viewModel.content().selection.start)
        assertEquals(30, viewModel.content().selection.end)

        viewModel.findUp("abc")
        assertEquals(7, viewModel.content().selection.start)
        assertEquals(10, viewModel.content().selection.end)
    }

    @Test
    fun findDown() {
        viewModel.content().clearText()
        viewModel.content().edit {
            append("abc is abc, you don't find abcd.")
            selection = TextRange.Zero
        }

        viewModel.findDown("abc")
        assertEquals(0, viewModel.content().selection.start)
        assertEquals(3, viewModel.content().selection.end)

        viewModel.findDown("abc")
        assertEquals(7, viewModel.content().selection.start)
        assertEquals(10, viewModel.content().selection.end)
    }

}