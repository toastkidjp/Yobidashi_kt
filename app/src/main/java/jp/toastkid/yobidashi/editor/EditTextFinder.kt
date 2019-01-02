/*
 * Copyright (c) 2018 toastkidjp.
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
class EditTextFinder(private val editText: EditText) {

    /**
     * Last index of find-text.
     */
    private var lastIndex = 0

    fun findUp(text: String) {
        if (lastIndex >= 0) {
            selectTextByIndex(findBackwardIndex(text), text);
        }
        val nextBackwardIndex = findBackwardIndex(text)
        if (nextBackwardIndex == -1) {
            lastIndex = editText.text.length
        }
    }

    private fun findBackwardIndex(text: String): Int {
        val index = lastIndex - text.length - 1
        if (index < 0) {
            return -1
        }
        val haystack = editText.text.toString()
        return haystack.lastIndexOf(text, index)
    }

    fun findDown(text: String) {
        selectTextByIndex(findNextForwardIndex(text), text)
        val nextForwardIndex = findNextForwardIndex(text)
        if (nextForwardIndex == -1) {
            lastIndex = 0
        }
    }

    private fun selectTextByIndex(index: Int, text: String) {
        if (index < 0) {
            lastIndex = 0
            return
        }
        requestFocusInputArea()
        lastIndex = index + text.length
        editText.setSelection(index, lastIndex)
    }

    private fun findNextForwardIndex(text: String) =
            editText.text.indexOf(text, lastIndex)

    private fun requestFocusInputArea() {
        editText.requestFocus()
    }

}