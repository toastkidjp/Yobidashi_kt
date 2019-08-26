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
 * TODO write test
 * @author toastkidjp
 */
class SelectedTextExtractor(private val editText: EditText) {

    operator fun invoke(): String {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        if (selectionStart == -1 || selectionEnd == -1) {
            return editText.text.toString()
        }
        return editText.text.substring(selectionStart, selectionEnd)
    }
}