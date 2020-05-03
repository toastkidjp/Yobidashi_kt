/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.rss.extractor

import android.view.View
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class RssUrlFinder(private val preferenceApplier: PreferenceApplier) {

    private val urlValidator = RssUrlValidator()

    private val rssUrlExtractor = RssUrlExtractor()

    operator fun invoke(
            currentUrl: String?,
            snackbarParentSupplier: () -> View?
    ) {
        val snackbarParent = snackbarParentSupplier() ?: return
        val colorPair = preferenceApplier.colorPair()

        if (currentUrl?.isNotBlank() == true && urlValidator(currentUrl)) {
            preferenceApplier.saveNewRssReaderTargets(currentUrl)
            Toaster.snackShort(snackbarParent, "Added $currentUrl", colorPair)
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val rssItems = withContext(Dispatchers.IO) {
                val response = HtmlApi().invoke(currentUrl)
                        ?: return@withContext emptyList<String>()
                if (!response.isSuccessful) {
                    return@withContext emptyList<String>()
                }
                rssUrlExtractor(response.body()?.string())
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
                    Toaster.snackShort(snackbarParent, "Added $it", colorPair)
                    return
                }

        Toaster.snackShort(snackbarParent, R.string.message_failure_extracting_rss, colorPair)
    }
}