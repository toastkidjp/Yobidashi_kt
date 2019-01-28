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
 * Text finder for [EditText].
 *
 * @param editText NonNull [EditText]
 * @author toastkidjp
 */
class EditTextFinder(private val editText: EditText) {

    /**
     * Last index of find-text.
     */
    private var lastIndex = 0

    /**
     * Find text in bound to upward.
     *
     * @param text finding text
     */
    fun findUp(text: String) {
        if (lastIndex <= 0) {
            lastIndex = editText.text.length
        }

        selectTextByIndex(findBackwardIndex(text), text);

        val nextBackwardIndex = findBackwardIndex(text)
        if (nextBackwardIndex == -1) {
            lastIndex = editText.text.length
        }
    }

    /**
     * Find text index in bound to upward.
     *
     * @param text finding text
     * @param index or -1
     */
    private fun findBackwardIndex(text: String): Int {
        val index = lastIndex - text.length - 1
        if (index < 0) {
            return -1
        }
        val haystack = editText.text.toString()
        return haystack.lastIndexOf(text, index)
    }

    /**
     * Find text in bound to downward.
     *
     * @param text finding text
     */
    fun findDown(text: String) {
        selectTextByIndex(findNextForwardIndex(text), text)
        val nextForwardIndex = findNextForwardIndex(text)
        if (nextForwardIndex == -1) {
            lastIndex = 0
        }
    }

    /**
     * Select text.
     *
     * @param index First index
     * @param text finding text
     */
    private fun selectTextByIndex(index: Int, text: String) {
        if (index < 0) {
            lastIndex = 0
            return
        }
        requestFocusInputArea()
        lastIndex = index + text.length
        editText.setSelection(index, lastIndex)
    }

    /**
     * Find next index in bound to forward.
     *
     * @param text Finding text
     * @return index of text in [EditText]
     */
    private fun findNextForwardIndex(text: String) =
            editText.text.indexOf(text, lastIndex)

    /**
     * Request focus for operating [EditText].
     */
    private fun requestFocusInputArea() {
        editText.requestFocus()
    }

}