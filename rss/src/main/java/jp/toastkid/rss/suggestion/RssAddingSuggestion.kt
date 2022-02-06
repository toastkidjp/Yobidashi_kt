/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.rss.suggestion

import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.rss.R
import jp.toastkid.rss.extractor.RssUrlValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class RssAddingSuggestion(
    private val preferenceApplier: PreferenceApplier,
    private val rssUrlValidator: RssUrlValidator = RssUrlValidator(),
    private val contentViewModelFactory: (ViewModelStoreOwner) -> ContentViewModel? = {
        ViewModelProvider(it).get(ContentViewModel::class.java)
    },
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    operator fun invoke(view: View, url: String) {
        CoroutineScope(mainDispatcher).launch {
            val shouldShow = withContext(backgroundDispatcher) { shouldShow(url) }
            if (!shouldShow) {
                return@launch
            }

            toast(view, url)
        }
    }

    private fun toast(view: View, url: String) {
        (view.context as? FragmentActivity)?.let {
            contentViewModelFactory(it)
                ?.snackWithAction(
                    view.context.getString(R.string.message_add_rss_target),
                    view.context.getString(R.string.add),
                    { preferenceApplier.saveNewRssReaderTargets(url) }
                )
        }
    }

    private fun shouldShow(url: String) =
            rssUrlValidator.invoke(url)
                    && !preferenceApplier.containsRssTarget(url)
}