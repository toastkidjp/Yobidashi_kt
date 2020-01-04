/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.rss.suggestion

import android.view.View
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.rss.extractor.RssUrlValidator
import timber.log.Timber

/**
 * @author toastkidjp
 */
class RssAddingSuggestion(private val preferenceApplier: PreferenceApplier) {

    private val rssUrlValidator = RssUrlValidator()

    operator fun invoke(view: View, url: String): Disposable =
            Maybe.fromCallable { shouldShow(url) }
                    .subscribeOn(Schedulers.computation())
                    .filter { it }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { toast(view, url) },
                            Timber::e
                    )

    private fun toast(view: View, url: String) {
        Toaster.snackLong(
                view,
                "Would you add this RSS?",
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