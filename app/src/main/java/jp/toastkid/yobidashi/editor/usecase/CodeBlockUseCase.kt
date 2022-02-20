/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.editor.usecase

import android.widget.EditText

class CodeBlockUseCase {

    private val lineSeparator = System.lineSeparator()

    operator fun invoke(editText: EditText, text: String) {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd

        editText.text.replace(
            selectionStart,
            selectionEnd,
            "```$lineSeparator$text$lineSeparator```"
        )
    }
}