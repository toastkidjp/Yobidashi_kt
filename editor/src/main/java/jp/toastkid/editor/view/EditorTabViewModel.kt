/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.view

import android.text.format.DateFormat
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.insert
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.text.MultiParagraph
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import jp.toastkid.editor.view.style.TextEditorOutputTransformation
import jp.toastkid.lib.preference.PreferenceApplier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max
import kotlin.math.min

class EditorTabViewModel {

    private val content = TextFieldState()

    private var lastParagraph: MultiParagraph? = null

    private val lineCount = mutableIntStateOf(0)

    private val lineNumberScrollState = ScrollState(0)

    private val focusRequester = FocusRequester()

    private val darkMode = mutableStateOf(true)

    private val openConfirmDialog = mutableStateOf(false)

    private val nestedScrollDispatcher = NestedScrollDispatcher()

    fun content() = content

    fun onValueChange() {
        applyStyle()
    }

    fun clearText() {
        content.clearText()
    }

    private val contentLength = mutableIntStateOf(0)

    fun contentLength(): Int = contentLength.intValue

    fun applyStyle() {
        if (contentLength.intValue != content.text.length) {
            contentLength.intValue = content.text.length
        }
    }

    fun setMultiParagraph(multiParagraph: MultiParagraph) {
        lastParagraph = multiParagraph
        if (lineCount.intValue != multiParagraph.lineCount) {
            lineCount.intValue = multiParagraph.lineCount
            val max = lineCount.intValue
            val length = max.toString().length
            val list = (1..max).map {
                val lineNumberCount = it
                val fillCount = length - lineNumberCount.toString().length
                return@map (it - 1 to with(StringBuilder()) {
                    repeat(fillCount) {
                        append(" ")
                    }
                    append(lineNumberCount)
                }.toString())
            }

            lineNumbers.value = list
        }
    }

    fun lineNumberScrollState() = lineNumberScrollState

    fun onClickLineNumber(it: Int) {
        val multiParagraph = lastParagraph ?: return

        content.edit {
            selection = TextRange(multiParagraph.getLineStart(it), multiParagraph.getLineEnd(it))
        }
    }

    fun focusRequester() = focusRequester

    fun initialScroll(coroutineScope: CoroutineScope, ms: Long = 500) {
        coroutineScope.launch {
            delay(ms)
            focusRequester().requestFocus()
        }
    }

    private val lineNumbers = mutableStateOf(listOf<Pair<Int, String>>())

    fun lineNumbers(): List<Pair<Int, String>> {
        return lineNumbers.value
    }

    fun currentLineOffset(): Offset {
        val paragraph = lastParagraph ?: return Offset.Unspecified
        val currentLine = paragraph.getLineForOffset(content.selection.start)
        return Offset(
            paragraph.getLineLeft(currentLine),
            paragraph.getLineTop(currentLine) - lineNumberScrollState.value
        )
    }

    fun currentLineHighlightColor(): Color {
        return Color(
            if (darkMode.value) 0xCC666239
            else 0xCCFFF9AF
        )
    }

    private val visualTransformation = TextEditorOutputTransformation(content, darkMode.value)

    fun visualTransformation(): OutputTransformation {
        return visualTransformation
    }

    fun dispose() {
        lastParagraph = null
        content.clearText()
    }

    fun insertText(
        primary: CharSequence
    ) {
        this.content.edit { insert(content.selection.start, primary.toString()) }
    }

    fun replaceText(primary: CharSequence) {
        val content = content()
        val start = min(content.selection.start, content.selection.end)
        val end = max(content.selection.start, content.selection.end)
        content.edit {
            replace(start, end, primary.toString())
        }
        applyStyle()
    }

    fun findUp(text: String) {
        val content = content()
        val index = content.text.lastIndexOf(text, content.selection.start - 1)
        if (index == -1) {
            return
        }
        this.content.edit {
            selection = TextRange(index, index + text.length)
        }
    }

    fun findDown(text: String) {
        val content = content()
        val index = content.text.indexOf(text, content.selection.end)
        if (index == -1) {
            return
        }
        this.content.edit {
            selection = TextRange(index, index + text.length)
        }
    }

    fun duplicateCurrentLine() {
        val textLayoutResult = lastParagraph ?: return
        val currentContent = content()
        val currentLine = textLayoutResult.getLineForOffset(currentContent.selection.start)
        val lineStart = textLayoutResult.getLineStart(currentLine)
        val lineEnd = textLayoutResult.getLineEnd(currentLine)
        val safeEnd = min(currentContent.text.length, lineEnd)

        this.content.edit {
            insert(safeEnd, "\n${currentContent.text.substring(lineStart, safeEnd)}")
        }
    }

    fun deleteCurrentLine() {
        val textLayoutResult = lastParagraph ?: return
        val currentContent = content()
        val currentLine = textLayoutResult.getLineForOffset(currentContent.selection.start)
        val lineStart = textLayoutResult.getLineStart(currentLine)
        val lineEnd = textLayoutResult.getLineEnd(currentLine)
        val targetEnd = min(currentContent.text.length, lineEnd + 1)

        this.content.edit {
            delete(lineStart, targetEnd)
        }
    }

    fun selectCurrentLine() {
        val textLayoutResult = lastParagraph ?: return
        val currentContent = content()
        val currentLine = textLayoutResult.getLineForOffset(currentContent.selection.start)
        val lineStart = textLayoutResult.getLineStart(currentLine)
        val lineEnd = textLayoutResult.getLineEnd(currentLine)
        val targetEnd = min(currentContent.text.length, lineEnd + 1)

        this.content.edit {
            selection = TextRange(lineStart, targetEnd)
        }
    }

    fun nestedScrollDispatcher() = nestedScrollDispatcher

    private val openInputFileNameDialog = mutableStateOf(false)

    fun openInputFileNameDialog() {
        openInputFileNameDialog.value = true
    }

    fun isOpenInputFileNameDialog(): Boolean {
        return openInputFileNameDialog.value
    }

    fun closeInputFileNameDialog() {
        openInputFileNameDialog.value = false
    }

    fun isOpenConfirmDialog(): Boolean {
        return openConfirmDialog.value
    }

    fun openConfirmDialog() {
        openConfirmDialog.value = true
    }

    fun closeConfirmDialog() {
        openConfirmDialog.value = false
    }

    private val exitDialogState = mutableStateOf(false)

    fun isOpenExitDialog(): Boolean {
        return exitDialogState.value
    }

    fun openExitDialog() {
        exitDialogState.value = true
    }

    fun closeExitDialog() {
        exitDialogState.value = false
    }

    private val openLoadFromStorageDialog = mutableStateOf(false)

    fun isOpenLoadFromStorageDialog(): Boolean {
        return openLoadFromStorageDialog.value
    }

    fun openLoadFromStorageDialog() {
        openLoadFromStorageDialog.value = true
    }

    fun closeLoadFromStorageDialog() {
        openLoadFromStorageDialog.value = false
    }

    private val lastSaved = mutableLongStateOf(0L)

    fun lastSaved(): CharSequence {
        return DateFormat.format(" HH:mm:ss", lastSaved.longValue)
    }

    fun setLastSaved(lastSaved: Long) {
        this.lastSaved.longValue = lastSaved
    }

    fun selectedText(): String {
        val selection = this.content.selection
        val start = min(selection.start, selection.end)
        val end = max(selection.start, selection.end)
        return content.text.substring(start, end)
    }

    fun selectToEnd() {
        this.content.edit {
            selection = TextRange(selection.start, content.text.length)
        }
    }

    suspend fun scrollToTop() {
        this.content.edit {
            selection = TextRange.Zero
        }

        lineNumberScrollState.scrollTo(0)
    }

    suspend fun scrollToBottom() {
        this.content.edit {
            selection = TextRange(content.text.length)
        }

        lineNumberScrollState.scrollTo(lineNumberScrollState.maxValue)
    }

    private val fontColor = mutableStateOf(Color.Transparent)

    fun fontColor(): Color = fontColor.value

    private val fontSize = AtomicReference(14.sp)

    fun fontSize(): TextUnit = fontSize.get()

    private val backgroundColor = AtomicReference(Color.Transparent)

    fun backgroundColor(): Color = backgroundColor.get()

    private val cursorColor = AtomicReference(Color.Transparent)

    fun cursorColor(): Color = cursorColor.get()

    fun setPreference(preferenceApplier: PreferenceApplier) {
        fontSize.set(preferenceApplier.editorFontSize().sp)
        backgroundColor.set(Color(preferenceApplier.editorBackgroundColor()))
        cursorColor.set(Color(preferenceApplier.editorCursorColor(Color.Cyan.toArgb())))

        darkMode.value = preferenceApplier.useDarkMode()
        fontColor.value = Color(preferenceApplier.editorFontColor())
    }

}