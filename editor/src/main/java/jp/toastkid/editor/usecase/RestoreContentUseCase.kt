/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.usecase

import android.widget.EditText
import jp.toastkid.editor.ContentHolderService
import jp.toastkid.editor.R
import jp.toastkid.lib.ContentViewModel

class RestoreContentUseCase(
    private val contentHolderService: ContentHolderService,
    private val contentViewModel: ContentViewModel?,
    private val editorInput: EditText,
    private val setContentText: (String) -> Unit
) {

    operator fun invoke() {
        if (contentHolderService.isBlank()) {
            contentViewModel?.snackShort(R.string.message_backup_is_empty)
            return
        }

        val selectionStart = editorInput.selectionStart
        val contentStr = contentHolderService.getContent()
        setContentText(contentStr)
        editorInput.setSelection(
            if (contentStr.length <= selectionStart) contentStr.length else selectionStart
        )
    }

}