/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.editor.usecase

import android.widget.EditText

class CurrentLineSelectionUseCase {

    /**
     * Select current line.
     *
     * @param editText [EditText]
     */
    operator fun invoke(editText: EditText) {
        val lineNumber = editText.layout.getLineForOffset(editText.selectionStart)
        val start = editText.layout.getLineStart(lineNumber)
        val end = editText.layout.getLineEnd(lineNumber)
        if (start < 0 || end < 0) {
            return
        }

        editText.post {
            editText.setSelection(start, end - 1)
        }
    }

}