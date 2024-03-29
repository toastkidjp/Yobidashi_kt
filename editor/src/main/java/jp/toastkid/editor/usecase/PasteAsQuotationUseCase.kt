/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.editor.usecase

import android.widget.EditText
import jp.toastkid.editor.Quotation
import jp.toastkid.editor.R
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.clip.Clipboard

/**
 * @author toastkidjp
 */
class PasteAsQuotationUseCase(
    private val editText: EditText,
    private val contentViewModel: ContentViewModel
) {

    operator fun invoke() {
        val context = editText.context
        val primary = Clipboard.getPrimary(context)
        if (primary.isNullOrEmpty()) {
            return
        }

        val currentText = editText.text.toString()
        val currentCursor = editText.selectionStart

        editText.text.insert(
            editText.selectionStart,
            Quotation()(primary)
        )

        contentViewModel
            .snackWithAction(
                context.getString(R.string.paste_as_quotation),
                context.getString(R.string.undo)
            ) {
                editText.setText(currentText)
                editText.setSelection(currentCursor)
            }
    }

}