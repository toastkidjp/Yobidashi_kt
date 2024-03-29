/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.usecase

import android.widget.EditText
import java.text.DecimalFormat

class ThousandSeparatorInsertionUseCase {

    private val formatter = DecimalFormat("#,###.##")

    operator fun invoke(editText: EditText, text: String) {
        val args = text.toBigDecimalOrNull() ?: return
        editText.text.replace(
            editText.selectionStart,
            editText.selectionEnd,
            formatter.format(args)
        )
    }

}