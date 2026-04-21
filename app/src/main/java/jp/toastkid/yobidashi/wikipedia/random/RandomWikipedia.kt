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
import jp.toastkid.api.wikipedia.WikipediaApi
import jp.toastkid.yobidashi.wikipedia.UrlDecider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * @author toastkidjp
 */
class RandomWikipedia(
    private val wikipediaApi: WikipediaApi = WikipediaApi(UrlDecider()()),
    private val urlDecider: UrlDecider = UrlDecider(),
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun fetchWithAction(titleAndLinkConsumer: (String, Uri) -> Unit) {
        CoroutineScope(mainDispatcher).launch {
            val title = withContext(ioDispatcher) {
                val titles = wikipediaApi.invoke()?.filter { it.ns == 0 } ?: return@withContext null
                titles[Random.nextInt(titles.size)].title
            } ?: return@launch

            titleAndLinkConsumer(title, "${urlDecider()}wiki/$title".toUri())
        }
    }

    companion object {

    }
}