/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.rss.extractor

import android.content.Context
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.api.html.HtmlApi
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.rss.R
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
    private val urlValidator: RssUrlValidator = RssUrlValidator(),
    private val rssUrlExtractor: RssUrlExtractor = RssUrlExtractor(),
    private val htmlApi: HtmlApi = HtmlApi(),
    private val contentViewModelFactory: (ViewModelStoreOwner) -> ContentViewModel? = {
        ViewModelProvider(it).get(ContentViewModel::class.java)
    },
    @VisibleForTesting
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    @VisibleForTesting
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {


    operator fun invoke(
            currentUrl: String?,
            snackbarParentSupplier: () -> View?
    ) {
        if (currentUrl.isNullOrBlank()) {
            return
        }

        val snackbarParent = snackbarParentSupplier() ?: return
        val colorPair = preferenceApplier.colorPair()

        if (urlValidator(currentUrl)) {
            preferenceApplier.saveNewRssReaderTargets(currentUrl)
            obtainContentViewModel(snackbarParent.context)
                    ?.snackShort("Added $currentUrl")
            return
        }

        CoroutineScope(mainDispatcher).launch {
            val rssItems = withContext(ioDispatcher) {
                val response = htmlApi.invoke(currentUrl)
                        ?: return@withContext emptyList<String>()
                if (!response.isSuccessful) {
                    return@withContext emptyList<String>()
                }
                rssUrlExtractor(response.body?.string())
            }
            storeToPreferences(rssItems, snackbarParent, colorPair)
        }
    }

    private fun storeToPreferences(
            urls: List<String>?,
            snackbarParent: View,
            colorPair: ColorPair
    ) {
        urls?.firstOrNull { urlValidator(it) }
                ?.let {
                    preferenceApplier.saveNewRssReaderTargets(it)
                    obtainContentViewModel(snackbarParent.context)?.snackShort("Added $it")
                    return
                }

        obtainContentViewModel(snackbarParent.context)
            ?.snackShort(R.string.message_failure_extracting_rss)
    }

    private fun obtainContentViewModel(context: Context) =
        (context as? FragmentActivity)?.let {
            contentViewModelFactory(it)
        }
}