/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.rss.suggestion

import android.view.View
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.rss.extractor.RssUrlValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class RssAddingSuggestion(private val preferenceApplier: PreferenceApplier) {

    private val rssUrlValidator = RssUrlValidator()

    operator fun invoke(view: View, url: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val shouldShow = withContext(Dispatchers.Default) { shouldShow(url) }
            if (!shouldShow) {
                return@launch
            }

            toast(view, url)
        }
    }


    private fun toast(view: View, url: String) {
        Toaster.snackLong(
                view,
                R.string.message_add_rss_target,
                R.string.title_add,
                View.OnClickListener {
                    preferenceApplier.saveNewRssReaderTargets(url)
                },
                preferenceApplier.colorPair()
        )
    }

    private fun shouldShow(url: String) =
            rssUrlValidator.invoke(url)
                    && !preferenceApplier.containsRssTarget(url)
}