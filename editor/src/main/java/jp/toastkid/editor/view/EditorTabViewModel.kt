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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.getSelectedText
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import jp.toastkid.editor.view.style.TextEditorVisualTransformation
import jp.toastkid.lib.preference.PreferenceApplier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max
import kotlin.math.min

class EditorTabViewModel {

    private val content = mutableStateOf(TextFieldValue())

    private var lastParagraph: MultiParagraph? = null

    private val lineCount = mutableIntStateOf(0)

    private val lineNumberScrollState = ScrollState(0)

    private val focusRequester = FocusRequester()

    private val darkMode = mutableStateOf(true)

    private val openConfirmDialog = mutableStateOf(false)

    private val nestedScrollDispatcher = NestedScrollDispatcher()

    fun content() = content.value

    fun onValueChange(it: TextFieldValue) {
        applyStyle(it)
    }

    fun clearText() {
        content.value = TextFieldValue()
    }

    private val contentLength = mutableIntStateOf(0)

    fun contentLength(): Int = contentLength.intValue

    private fun applyStyle(it: TextFieldValue) {
        content.value = it

        if (contentLength.intValue != content.value.text.length) {
            contentLength.intValue = content.value.text.length
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

        content.value = content.value.copy(
            selection = TextRange(multiParagraph.getLineStart(it), multiParagraph.getLineEnd(it))
        )
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

    fun launchTab(
        content: TextFieldValue
    ) {
        val newContent = content
        applyStyle(newContent)
    }

    fun currentLineOffset(): Offset {
        val paragraph = lastParagraph ?: return Offset.Unspecified
        val currentLine = paragraph.getLineForOffset(content.value.selection.start)
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

    private val visualTransformation = TextEditorVisualTransformation(content, darkMode.value)

    fun visualTransformation(): VisualTransformation {
        return visualTransformation
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
        val start = min(content.selection.start, content.selection.end)
        val end = max(content.selection.start, content.selection.end)
        onValueChange(
            content.copy(
                text = StringBuilder(content.text)
                    .replace(
                        start,
                        end,
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

    fun duplicateCurrentLine() {
        val textLayoutResult = lastParagraph ?: return
        val currentContent = content()
        val currentLine = textLayoutResult.getLineForOffset(currentContent.selection.start)
        val lineStart = textLayoutResult.getLineStart(currentLine)
        val lineEnd = textLayoutResult.getLineEnd(currentLine)
        val safeEnd = min(currentContent.text.length, lineEnd)
        val newText = StringBuilder(currentContent.text)
            .insert(safeEnd, "\n${currentContent.text.substring(lineStart, safeEnd)}")
            .toString()
        onValueChange(currentContent.copy(text = newText))
    }

    fun deleteCurrentLine() {
        val textLayoutResult = lastParagraph ?: return
        val currentContent = content()
        val currentLine = textLayoutResult.getLineForOffset(currentContent.selection.start)
        val lineStart = textLayoutResult.getLineStart(currentLine)
        val lineEnd = textLayoutResult.getLineEnd(currentLine)
        val targetEnd = min(currentContent.text.length, lineEnd + 1)
        val newText = StringBuilder(currentContent.text)
            .delete(lineStart, targetEnd)
            .toString()
        onValueChange(currentContent.copy(text = newText))
    }

    fun selectCurrentLine() {
        val textLayoutResult = lastParagraph ?: return
        val currentContent = content()
        val currentLine = textLayoutResult.getLineForOffset(currentContent.selection.start)
        val lineStart = textLayoutResult.getLineStart(currentLine)
        val lineEnd = textLayoutResult.getLineEnd(currentLine)
        val targetEnd = min(currentContent.text.length, lineEnd + 1)
        onValueChange(currentContent.copy(selection = TextRange(lineStart, targetEnd)))
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
        return content.value.getSelectedText().text
    }

    fun selectToEnd() {
        content.value = content.value.copy(selection = TextRange(content.value.selection.start, content.value.text.length))
    }

    suspend fun scrollToTop() {
        onValueChange(content.value.copy(selection = TextRange.Zero))

        lineNumberScrollState.scrollTo(0)
    }

    suspend fun scrollToBottom() {
        val textFieldValue = content.value
        onValueChange(textFieldValue.copy(selection = TextRange(textFieldValue.text.length)))

        lineNumberScrollState.scrollTo(lineNumberScrollState.maxValue)
    }

    private val fontColor = AtomicReference(Color.Transparent)

    fun fontColor(): Color = fontColor.get()

    private val fontSize = AtomicReference(14.sp)

    fun fontSize(): TextUnit = fontSize.get()

    private val backgroundColor = AtomicReference(Color.Transparent)

    fun backgroundColor(): Color = backgroundColor.get()

    private val cursorColor = AtomicReference(Color.Transparent)

    fun cursorColor(): Color = cursorColor.get()

    fun setPreference(preferenceApplier: PreferenceApplier) {
        fontColor.set(Color(preferenceApplier.editorFontColor()))
        fontSize.set(preferenceApplier.editorFontSize().sp)
        backgroundColor.set(Color(preferenceApplier.editorBackgroundColor()))
        cursorColor.set(Color(preferenceApplier.editorCursorColor(Color.Cyan.toArgb())))
        darkMode.value = preferenceApplier.useDarkMode()
    }

}