/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib.view.text

import android.content.Context
import android.text.InputType
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout

/**
 *
 * @param factory For injecting test dependencies.
 * @author toastkidjp
 */
class SingleLineTextInputLayoutFactory(
    private val factory: (Context) -> TextInputLayout = { TextInputLayout(it) }
) {

    /**
     * Make [TextInputLayout] instance.
     *
     * @param context [Context] Use for make instance.
     */
    operator fun invoke(context: Context): TextInputLayout =
        factory(context)
            .also { layout ->
                layout.addView(
                    EditText(context).also {
                        it.maxLines   = 1
                        it.inputType  = InputType.TYPE_CLASS_TEXT
                        it.imeOptions = EditorInfo.IME_ACTION_SEARCH
                    },
                    0,
                    LAYOUT_PARAMS
                )
            }

    companion object {

        /**
         * EditText's layout params.
         */
        private val LAYOUT_PARAMS = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

    }

}