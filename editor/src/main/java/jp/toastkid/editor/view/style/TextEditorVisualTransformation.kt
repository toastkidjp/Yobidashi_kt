/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.view.style

import androidx.compose.runtime.State
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import kotlin.math.min

class TextEditorVisualTransformation(
    private val content: State<TextFieldValue>,
    private val darkMode: Boolean
) : VisualTransformation {

    private val theme = EditorTheme()

    private var transformedText: TransformedText? = null

    override fun filter(text: AnnotatedString): TransformedText {
        val last = transformedText
        val substring = substring(last)
        if (last != null && content.value.composition == null && substring == text.text) {
            return last
        }

        val new = TransformedText(theme.codeString(text.text, darkMode), offsetMapping)
        transformedText = new
        return new
    }

    private fun substring(last: TransformedText?): String {
        if (last == null) {
            return ""
        }

        return last.text.text.substring(0, last.text.length - 5)
    }

    private val offsetMapping = object : OffsetMapping {

        override fun originalToTransformed(offset: Int): Int {
            return min(offset, content.value.text.length)
        }

        override fun transformedToOriginal(offset: Int): Int {
            return min(offset, content.value.text.length)
        }

    }

}