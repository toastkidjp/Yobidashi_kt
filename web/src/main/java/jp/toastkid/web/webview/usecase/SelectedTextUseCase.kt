/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.web.webview.usecase

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.web.R

class SelectedTextUseCase(
        private val stringResolver: (Int, Any) -> String,
        private val contentViewModel: ContentViewModel
) {

    fun countCharacters(word: String) {
        val codePointCount = word.codePointCount(1, word.length - 1)
        val message = stringResolver(R.string.message_character_count, codePointCount)
        contentViewModel.snackShort(message)
    }

    // TODO
    fun search(word: String) {
        contentViewModel.search(word)
    }

    fun searchWithPreview(word: String, searchEngine: String?) {
        contentViewModel.preview(word)
    }

    companion object {

        fun make(context: Context?): SelectedTextUseCase? =
            (context as? ViewModelStoreOwner)?.let { activity ->
                val viewModelProvider = ViewModelProvider(activity)
                return SelectedTextUseCase(
                    stringResolver = { resource, additional -> context.getString(resource, additional) },
                    contentViewModel = viewModelProvider.get(ContentViewModel::class.java)
                )
            }

    }

}