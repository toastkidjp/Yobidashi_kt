/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.editor

import android.widget.EditText

/**
 * @author toastkidjp
 */
class StringSurroundingUseCase {

    operator fun invoke(editText: EditText, character: Char) {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        val text = editText.text.substring(selectionStart, selectionEnd)

        editText.text.replace(
                selectionStart,
                selectionEnd,
                "$character$text$character"
        )
    }
}