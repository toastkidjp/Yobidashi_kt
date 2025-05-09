/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.web.rss.extractor

import jp.toastkid.api.html.HtmlApi
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.web.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class RssUrlFinder(
    private val preferenceApplier: PreferenceApplier,
    private val contentViewModel: ContentViewModel,
    private val urlValidator: RssUrlValidator = RssUrlValidator(),
    private val rssUrlExtractor: RssUrlExtractor = RssUrlExtractor(),
    private val htmlApi: HtmlApi = HtmlApi(),
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {


    operator fun invoke(
        currentUrl: String?
    ) {
        if (currentUrl.isNullOrBlank()) {
            return
        }

        if (urlValidator(currentUrl)) {
            preferenceApplier.saveNewRssReaderTargets(currentUrl)
            contentViewModel.snackShort("Added $currentUrl")
            return
        }

        CoroutineScope(mainDispatcher).launch {
            val rssItems = withContext(ioDispatcher) {
                val response = htmlApi(currentUrl)
                        ?: return@withContext emptyList<String>()
                rssUrlExtractor(response)
            }
            storeToPreferences(rssItems)
        }
    }

    private fun storeToPreferences(urls: List<String>?) {
        urls?.firstOrNull { urlValidator(it) }
                ?.let {
                    preferenceApplier.saveNewRssReaderTargets(it)
                    contentViewModel.snackShort("Added $it")
                    return
                }

        contentViewModel.snackShort(R.string.message_failure_extracting_rss)
    }

}