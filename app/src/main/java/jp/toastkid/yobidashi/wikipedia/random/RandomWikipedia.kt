/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.wikipedia.random

import android.net.Uri
import androidx.core.net.toUri
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import kotlin.random.Random

/**
 * @author toastkidjp
 */
class RandomWikipedia {

    private val wikipediaApi = WikipediaApi()

    private val urlDecider = UrlDecider()

    fun fetchWithAction(titleAndLinkConsumer: (String, Uri) -> Unit): Disposable =
            Maybe.fromCallable {
                val titles = wikipediaApi.invoke()?.filter { it.ns == 0 }
                        ?: throw NullPointerException()
                return@fromCallable titles[Random.nextInt(titles.size)].title
            }
                    .retry(3)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { titleAndLinkConsumer(it, "${urlDecider()}wiki/$it".toUri()) },
                            Timber::e
                    )
}