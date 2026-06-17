/*
 * Copyright (c) 2025 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.editor.view.style

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import java.util.regex.Pattern

@Immutable
data class EditorStylePattern(
    val regex: Pattern,
    val lightStyle: SpanStyle,
    val darkStyle: SpanStyle
)

/**
 * Data class for keeping calculation result.
 */
data class ParseResult(
    val text: String,
    val styles: List<Triple<Int, Int, SpanStyle>>
)

class TextEditorOutputTransformation(
    private val content: TextFieldState,
    private val darkMode: Boolean,
    private val parseResultProvider: () -> ParseResult
) : OutputTransformation {

    private val patterns = listOf(
        EditorStylePattern(
            Pattern.compile("^[0-9]+\\.\\s", Pattern.MULTILINE),
            SpanStyle(Color(0xFF6897BB)),
            SpanStyle(Color(0xFFA8B7EE))
        ),
        EditorStylePattern(
            Pattern.compile("^#.*?$", Pattern.MULTILINE),
            SpanStyle(Color(0xFF008800), fontWeight = FontWeight.Bold),
            SpanStyle(Color(0xFF00DD00), fontWeight = FontWeight.Bold)
        ),
        EditorStylePattern(
            Pattern.compile("^\\|.*?$", Pattern.MULTILINE),
            SpanStyle(Color(0xFF8800CC)),
            SpanStyle(Color(0xFF86EEC7))
        ),
        EditorStylePattern(
            Pattern.compile("^>.*?$", Pattern.MULTILINE),
            SpanStyle(Color(0xFF7744AA)),
            SpanStyle(Color(0xFFCCAAFF))
        ),
        EditorStylePattern(
            Pattern.compile("^-.*?$", Pattern.MULTILINE),
            SpanStyle(Color(0xFF666239)),
            SpanStyle(Color(0xFFFFD54F))
        ),
        EditorStylePattern(
            Pattern.compile("^\\*.*?$", Pattern.MULTILINE),
            SpanStyle(Color(0xFF666239)),
            SpanStyle(Color(0xFFFFD54F))
        )
    )

    override fun TextFieldBuffer.transformOutput() {
        val currentText = this.asCharSequence()
        val currentParseResult = parseResultProvider()
        val parsedText = currentParseResult.text

        if (currentText == parsedText) {
            currentParseResult.styles.forEach { (start, end, style) ->
                if (start <= length && end <= length) {
                    addStyle(style, start, end)
                }
            }

            if (content.composition == null) {
                append(END_OF_FILE_MARKER)
            }

            return
        }

        val diffIndex = findDiffIndexFast(currentText, parsedText)
        val realDiffLength = currentText.length - parsedText.length

        currentParseResult.styles.forEach { (start, end, style) ->
            var newStart = start
            var newEnd = end

            if (start >= diffIndex) {
                newStart = (start + realDiffLength).coerceAtLeast(0)
                newEnd = (end + realDiffLength).coerceAtLeast(0)
            } else if (end > diffIndex) {
                newEnd = (end + realDiffLength).coerceAtLeast(0)
            }

            if (newStart < newEnd && newStart <= length && newEnd <= length) {
                addStyle(style, newStart, newEnd)
            }
        }

        val selectionStart = content.selection.start
        if (selectionStart <= currentText.length) {
            val (lineStart, lineEnd) = findCurrentLineRange(currentText, selectionStart)
            val currentLineText = currentText.substring(lineStart, lineEnd)

            patterns.forEach { pattern ->
                val matcher = pattern.regex.matcher(currentLineText)
                while (matcher.find()) {
                    val style = if (darkMode) pattern.darkStyle else pattern.lightStyle

                    val globalStart = lineStart + matcher.start()
                    val globalEnd = lineStart + matcher.end()

                    if (globalStart <= length && globalEnd <= length) {
                        addStyle(style, globalStart, globalEnd)
                    }
                }
            }
        }

        if (content.composition == null) {
            append(END_OF_FILE_MARKER)
        }
    }

    private fun findDiffIndexFast(current: CharSequence, parsed: String): Int {
        val minLen = minOf(current.length, parsed.length)
        for (i in 0 until minLen) {
            if (current[i] != parsed[i]) {
                return i
            }
        }
        return minLen
    }

    private fun findCurrentLineRange(text: CharSequence, selectionStart: Int): Pair<Int, Int> {
        val start = text.lastIndexOf('\n', selectionStart - 1).coerceAtKeyAtLeast(0)
        val end = text.indexOf('\n', selectionStart).let { if (it == -1) text.length else it }
        return Pair(start, end)
    }

    private fun Int.coerceAtKeyAtLeast(value: Int): Int = if (this < value) value else this

    fun getPatterns() = patterns

}

private const val END_OF_FILE_MARKER = "[EOF]"
