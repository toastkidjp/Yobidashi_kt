/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.editor.usecase

import android.widget.EditText
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.clip.Clipboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LinkFormInsertionUseCase(
    private val editText: EditText,
    private val contentViewModel: ContentViewModel
) {

    operator fun invoke() {
        val context = editText.context
        val primary = Clipboard.getPrimary(context)?.toString()
        if (primary.isNullOrEmpty() || Urls.isInvalidUrl(primary)) {
            return
        }

        val currentText = editText.text.toString()
        val currentCursor = editText.selectionStart

        CoroutineScope(Dispatchers.Main).launch {
            val linkWithTitle = withContext(Dispatchers.IO) {
                LinkTitleFetcherUseCase()(primary)
            }
            editText.text.insert(editText.selectionStart, linkWithTitle)
        }

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