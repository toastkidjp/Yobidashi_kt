/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib.view.text

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import jp.toastkid.lib.R

/**
 * @author toastkidjp
 */
class EmptyAlertSetter {

    /**
     * Set empty alert.
     *
     * @param inputLayout [TextInputLayout]
     */
    operator fun invoke(inputLayout: TextInputLayout): EditText {
        val input: EditText? = inputLayout.editText

        input?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable) {
                if (s.toString().isEmpty()) {
                    inputLayout.error = inputLayout.context
                        .getString(R.string.favorite_search_addition_dialog_empty_message)
                    return
                }
                inputLayout.isErrorEnabled = false
            }
        })
        return input ?: EditText(inputLayout.context)
    }

}