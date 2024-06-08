/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.view.style

import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

class EditorTheme {

    private val patterns = listOf(
        EditorStyle(
            Regex("[0-9]*"),
            SpanStyle(Color(0xFF6897BB)),
            SpanStyle(Color(0xFFA8B7EE))
        ),
        EditorStyle(
            Regex("^#.*"),
            SpanStyle(Color(0xFF008800), fontWeight = FontWeight.Bold),
            SpanStyle(Color(0xFF00DD00), fontWeight = FontWeight.Bold)
        ),
        EditorStyle(
            Regex("\\n#.*"),
            SpanStyle(Color(0xFF008800), fontWeight = FontWeight.Bold),
            SpanStyle(Color(0xFF00DD00), fontWeight = FontWeight.Bold)
        ),
        EditorStyle(
            Regex("\\n\\|.*"),
            SpanStyle(Color(0xFF8800CC)),
            SpanStyle(Color(0xFF86EEC7))
        ),
        EditorStyle(
            Regex("\\n>.*"),
            SpanStyle(Color(0xFF7744AA)),
            SpanStyle(Color(0xFFCCAAFF))
        ),
        EditorStyle(
            Regex("\\n-.*"),
            SpanStyle(Color(0xFF666239)),
            SpanStyle(Color(0xFFFFD54F))
        )
    )

    fun codeString(str: String, darkTheme: Boolean) = buildAnnotatedString {
        append(str)
        appendInlineContent("EOF", "[EOF]")

        patterns.forEach {
            addStyle(if (darkTheme) it.darkStyle else it.lightStyle, str, it.regex)
        }
    }

    private fun AnnotatedString.Builder.addStyle(style: SpanStyle, text: String, regexp: Regex) {
        for (result in regexp.findAll(text)) {
            addStyle(style, result.range.first, result.range.last + 1)
        }
    }

}