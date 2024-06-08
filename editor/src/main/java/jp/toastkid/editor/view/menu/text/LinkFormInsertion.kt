/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.view.menu.text

import android.content.Context
import jp.toastkid.editor.R
import jp.toastkid.editor.usecase.LinkTitleFetcherUseCase
import jp.toastkid.editor.view.EditorTabViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.clip.Clipboard
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LinkFormInsertion(
    private val context: Context,
    private val viewModel: EditorTabViewModel,
    private val contentViewModel: ContentViewModel,
    private val linkTitleFetcherUseCase: LinkTitleFetcherUseCase = LinkTitleFetcherUseCase(),
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    operator fun invoke() {
        val primary = Clipboard.getPrimary(context)?.toString()
        if (primary.isNullOrEmpty() || Urls.isInvalidUrl(primary)) {
            return
        }

        val currentText = viewModel.content().copy()

        CoroutineScope(mainDispatcher).launch {
            val linkWithTitle = withContext(ioDispatcher) {
                linkTitleFetcherUseCase(primary)
            }
            viewModel.insertText(linkWithTitle)

            contentViewModel
                .snackWithAction(
                    context.getString(R.string.done_addition),
                    context.getString(R.string.undo)
                ) {
                    viewModel.onValueChange(currentText)
                }
        }
    }

}