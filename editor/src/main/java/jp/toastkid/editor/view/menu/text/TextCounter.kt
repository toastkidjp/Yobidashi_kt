/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.view.menu.text

import android.content.Context
import androidx.compose.ui.text.input.getSelectedText
import jp.toastkid.editor.R
import jp.toastkid.editor.view.EditorTabViewModel
import jp.toastkid.lib.ContentViewModel

class TextCounter {

    operator fun invoke(
        context: Context,
        viewModel: EditorTabViewModel,
        contentViewModel: ContentViewModel
    ) {
        val count = viewModel.content().getSelectedText().length
        contentViewModel.snackShort(
            context.getString(R.string.message_character_count, count)
        )
    }

}