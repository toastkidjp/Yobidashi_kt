/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.rss.extractor

import android.view.View
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import timber.log.Timber

/**
 * @author toastkidjp
 */
class RssUrlFinder(private val preferenceApplier: PreferenceApplier) {

    private val urlValidator = RssUrlValidator()

    operator fun invoke(
            currentUrl: String?,
            snackbarParentSupplier: () -> View?
    ): Disposable {
        val snackbarParent = snackbarParentSupplier() ?: return Disposables.disposed()
        val colorPair = preferenceApplier.colorPair()

        if (currentUrl?.isNotBlank() == true && urlValidator(currentUrl)) {
            preferenceApplier.saveNewRssReaderTargets(currentUrl)
            Toaster.snackShort(snackbarParent, "Added $currentUrl", colorPair)
            return Disposables.disposed()
        }

        return Maybe.fromCallable { HtmlApi().invoke(currentUrl) }
                .subscribeOn(Schedulers.io())
                .filter { it.isSuccessful }
                .map { RssUrlExtractor()(it.body()?.string()) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            it?.firstOrNull { urlValidator(it) }
                                    ?.let {
                                        preferenceApplier.saveNewRssReaderTargets(it)
                                        Toaster.snackShort(snackbarParent, "Added $it", colorPair)
                                        return@subscribe
                                    }
                            Toaster.snackShort(snackbarParent, R.string.message_failure_extracting_rss, colorPair)
                        },
                        Timber::e
                )
    }
}