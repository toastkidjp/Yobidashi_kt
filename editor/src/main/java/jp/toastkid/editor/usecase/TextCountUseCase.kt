/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.usecase

import android.widget.EditText
import jp.toastkid.lib.ContentViewModel

class TextCountUseCase {

    operator fun invoke(editText: EditText, contentViewModel: ContentViewModel?) {
        val count = editText.text.substring(editText.selectionStart, editText.selectionEnd).length
        contentViewModel?.snackShort(
            editText.context.getString(jp.toastkid.lib.R.string.message_character_count, count)
        )
    }

}