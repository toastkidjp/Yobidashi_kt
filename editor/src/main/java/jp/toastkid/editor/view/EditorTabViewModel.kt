/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.view

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.MultiParagraph
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import jp.toastkid.editor.view.style.TextEditorVisualTransformation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EditorTabViewModel {

    private val content = mutableStateOf(TextFieldValue())

    private var lastParagraph: MultiParagraph? = null

    private var altPressed = false

    private val lineCount = mutableStateOf(0)

    private val lineNumberScrollState = ScrollState(0)

    private val focusRequester = FocusRequester()

    private val darkMode = mutableStateOf(true)

    fun content() = content.value

    fun onValueChange(it: TextFieldValue) {
        if (altPressed) {
            return
        }

        applyStyle(it)
    }

    private fun applyStyle(it: TextFieldValue) {
        //val newContent = if (tab.editable()) it else it.copy(text = content.value.text)

        content.value = it
    }

    private val lineHeights = mutableMapOf<Int, TextUnit>()

    fun setMultiParagraph(multiParagraph: MultiParagraph) {
        lastParagraph = multiParagraph
        if (lineCount.value != multiParagraph.lineCount) {
            lineCount.value = multiParagraph.lineCount
        }

        val lastLineHeights = (0 until lineCount.value).map { it to multiParagraph.getLineHeight(it) }.toMap()
        val distinct = lastLineHeights.values.distinct()
        val max = distinct.max()
        lineHeights.clear()
        lastLineHeights.forEach { lineHeights.put(it.key, (1.55f * it.value / max).em) }
    }

    fun getLineHeight(lineNumber: Int): TextUnit {
        return 1.55.em//lineHeights.getOrElse(lineNumber, { 1.55.em })
    }

    fun lineNumberScrollState() = lineNumberScrollState

    fun onClickLineNumber(it: Int) {
        val multiParagraph = lastParagraph ?: return

        content.value = content.value.copy(
            selection = TextRange(multiParagraph.getLineStart(it), multiParagraph.getLineEnd(it))
        )
    }

    fun focusRequester() = focusRequester

    fun initialScroll(coroutineScope: CoroutineScope, ms: Long = 500) {
        /*if (tab.scroll() <= 0.0) {
            focusRequester().requestFocus()
            return
        }*/

        coroutineScope.launch {
            delay(ms)
            focusRequester().requestFocus()
        }
    }

    fun lineNumbers(): List<Pair<Int, String>> {
        val max = lineCount.value
        val length = max.toString().length
        return (1 .. max).map {
            val lineNumberCount = it
            val fillCount = length - lineNumberCount.toString().length
            return@map (it - 1 to with(StringBuilder()) {
                repeat(fillCount) {
                    append(" ")
                }
                append(lineNumberCount)
            }.toString())
        }
    }

    fun launchTab(
        content: TextFieldValue,
        useDarkMode: Boolean,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) {
        darkMode.value = useDarkMode

        val newContent = content
        applyStyle(newContent)

        /*TODO CoroutineScope(dispatcher).launch {
            mainViewModel.finderFlow().collect {
                findOrderReceiver(it, content.value) { content.value = it }
            }
        }*/
    }

    fun currentLineOffset(): Offset {
        val paragraph = lastParagraph ?: return Offset.Unspecified
        val currentLine = paragraph.getLineForOffset(content.value.selection.start)
        return Offset(
            paragraph.getLineLeft(currentLine),
            paragraph.getLineTop(currentLine) //TODO - verticalScrollState.offset
        )
    }

    fun currentLineHighlightColor(): Color {
        return Color(
            if (darkMode.value) 0xCC666239
            else 0xCCFFF9AF
        )
    }

    private val visualTransformation = TextEditorVisualTransformation(content, darkMode.value)

    fun visualTransformation(): VisualTransformation {
        return visualTransformation
    }

    fun makeCharacterCountMessage(count: Int): String {
        return "Character: $count"
    }

    fun dispose() {
        lastParagraph = null
        content.value = TextFieldValue()
    }

    fun insertText(
        primary: CharSequence
    ) {
        val content = content()
        onValueChange(
            content.copy(
                text = StringBuilder(content.text)
                    .replace(
                        content.selection.start,
                        content.selection.start,
                        primary.toString()
                    )
                    .toString()
            )
        )
    }

    fun replaceText(primary: CharSequence) {
        val content = content()
        onValueChange(
            content.copy(
                text = StringBuilder(content.text)
                    .replace(
                        content.selection.start,
                        content.selection.end,
                        primary.toString()
                    )
                    .toString()
            )
        )
    }

    fun findUp(text: String) {
        val content = content()
        val index = content.text.lastIndexOf(text, content.selection.start - 1)
        if (index == -1) {
            return
        }
        this.content.value = content.copy(selection = TextRange(index, index + text.length))
    }

    fun findDown(text: String) {
        val content = content()
        val index = content.text.indexOf(text, content.selection.end)
        if (index == -1) {
            return
        }
        this.content.value = content.copy(selection = TextRange(index, index + text.length))
    }

}