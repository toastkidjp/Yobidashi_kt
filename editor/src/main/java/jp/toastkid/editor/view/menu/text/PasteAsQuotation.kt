/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.view.menu.text

import android.content.Context
import jp.toastkid.editor.Quotation
import jp.toastkid.editor.R
import jp.toastkid.editor.view.EditorTabViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.clip.Clipboard

class PasteAsQuotation(
    private val context: Context,
    private val viewModel: EditorTabViewModel,
    private val contentViewModel: ContentViewModel
) {

    operator fun invoke() {
        val primary = Clipboard.getPrimary(context)
        if (primary.isNullOrEmpty()) {
            return
        }

        val currentText = viewModel.content().copy()

        val quotation = Quotation()(primary) ?: return
        viewModel.insertText(quotation)

        contentViewModel
            .snackWithAction(
                context.getString(R.string.paste_as_quotation),
                context.getString(R.string.undo)
            ) {
                viewModel.onValueChange(currentText)
            }
    }

}