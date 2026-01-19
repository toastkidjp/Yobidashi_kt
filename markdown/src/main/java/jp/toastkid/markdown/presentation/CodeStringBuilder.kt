/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.markdown.presentation

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import java.util.regex.Pattern

class CodeStringBuilder : OutputTransformation {

    private val value: SpanStyle = SpanStyle(Color(0xFF6897BB))

    private val keyword: SpanStyle = SpanStyle(Color(0xFFCC7832))

    private val punctuation: SpanStyle = SpanStyle(Color(0xFFA1C17E))

    private val punctuationPattern = Pattern.compile("[:=\"\\[\\]\\{\\}\\(\\),]")

    private val keywordPattern = Pattern.compile("\\b(fun|val|var|private|internal|for|expect|actual|import|package|static|object) ")

    private val valuePattern = Pattern.compile("(true|false)")

    private val digitPattern = Pattern.compile("[0-9]*")

    override fun TextFieldBuffer.transformOutput() {
        applyPattern(punctuationPattern, punctuation)
        applyPattern(keywordPattern, keyword)
        applyPattern(valuePattern, value)
        applyPattern(digitPattern, value)
    }

    private fun TextFieldBuffer.applyPattern(pattern: Pattern, style: SpanStyle) {
        val matcher = pattern.matcher(asCharSequence())
        while (matcher.find()) {
            addStyle(style, matcher.start(), matcher.end())
        }
    }

}