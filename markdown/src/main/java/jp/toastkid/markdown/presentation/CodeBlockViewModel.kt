/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.markdown.presentation


import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.MultiParagraph
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import kotlin.math.min

class CodeBlockViewModel {

    private val content = mutableStateOf(TextFieldValue())

    private val codeStringBuilder = CodeStringBuilder()

    private val lineCountState = mutableStateOf(1)

    fun maxHeight(fontSize: TextUnit): Dp {
        return min(lineCountState.value * fontSize.value * 1.55.em.value, 800f).dp
    }

    fun content() = content.value

    fun transform(it: AnnotatedString): TransformedText {
        val t = codeStringBuilder(it.text)
        return TransformedText(t, OffsetMapping.Identity)
    }

    fun onValueChange(it: TextFieldValue) {
        content.value = it
    }

    fun lineNumberTexts(): List<String> {
        val max = lineCountState.value
        val length = max.toString().length

        return (0 until max).map {
            val lineNumberCount = it + 1
            val fillCount = length - lineNumberCount.toString().length
            with(StringBuilder()) {
                repeat(fillCount) {
                    append(" ")
                }
                append(lineNumberCount)
            }.toString()
        }
    }

    fun start(code: String) {
        content.value = TextFieldValue(code)
    }

    fun setMultiParagraph(multiParagraph: MultiParagraph) {
        val lineCount = multiParagraph.lineCount
        lineCountState.value = lineCount
    }

}