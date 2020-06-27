/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.detail

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView

class PageSearcherModule(
    private val input: EditText,
    private val target: TextView
    ) {

    init {
        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                TextViewHighlighter(target, s.toString())
            }

            override fun afterTextChanged(s: Editable) = Unit
        })
    }

    /**
     * Implement for Data Binding.
     */
    fun find() {
        TextViewHighlighter(target, input.text.toString())
    }

    /**
     * Implement for Data Binding.
     */
    fun clearInput() {
        input.setText("")
        TextViewHighlighter(target, "")
    }
}