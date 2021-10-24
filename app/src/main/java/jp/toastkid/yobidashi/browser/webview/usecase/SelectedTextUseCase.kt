/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.webview.usecase

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.search.SearchCategory
import jp.toastkid.search.UrlFactory
import jp.toastkid.yobidashi.R

class SelectedTextUseCase(
        private val urlFactory: UrlFactory = UrlFactory(),
        private val stringResolver: (Int, Any) -> String,
        private val contentViewModel: ContentViewModel,
        private val browserViewModel: BrowserViewModel
) {

    fun countCharacters(word: String) {
        val codePointCount = word.codePointCount(1, word.length - 1)
        val message = stringResolver(R.string.message_character_count, codePointCount)
        contentViewModel.snackShort(message)
    }

    fun search(word: String, searchEngine: String?) {
        val url = calculateToUri(word, searchEngine) ?: return
        browserViewModel.open(url)
    }

    fun searchWithPreview(word: String, searchEngine: String?) {
        val url = calculateToUri(word, searchEngine) ?: return
        browserViewModel.preview(url)
    }

    private fun calculateToUri(word: String, searchEngine: String?): Uri? {
        val cleaned =
            if (word.startsWith("\"") && word.length > 10) word.substring(1, word.length - 2)
            else word

        return if (Urls.isValidUrl(cleaned)) cleaned.toUri()
        else makeUrl(word, searchEngine)
    }

    private fun makeUrl(word: String, searchEngine: String?): Uri? {
        if (word.isEmpty() || word == "\"\"") {
            contentViewModel.snackShort(R.string.message_failed_query_extraction_from_web_view)
            return null
        }

        return urlFactory(
                searchEngine ?: SearchCategory.getDefaultCategoryName(),
                word
        )
    }

    companion object {

        fun make(context: Context?): SelectedTextUseCase? =
            (context as? FragmentActivity)?.let { activity ->
                val viewModelProvider = ViewModelProvider(activity)
                return SelectedTextUseCase(
                    stringResolver = { resource, additional -> context.getString(resource, additional) },
                    contentViewModel = viewModelProvider.get(ContentViewModel::class.java),
                    browserViewModel = viewModelProvider.get(BrowserViewModel::class.java)
                )
            }

    }

}