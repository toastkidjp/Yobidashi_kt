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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * @author toastkidjp
 */
class RandomWikipedia {

    private val wikipediaApi = WikipediaApi()

    private val urlDecider = UrlDecider()

    fun fetchWithAction(titleAndLinkConsumer: (String, Uri) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            val title = withContext(Dispatchers.IO) {
                val titles = wikipediaApi.invoke()?.filter { it.ns == 0 } ?: return@withContext null
                titles[Random.nextInt(titles.size)].title
            } ?: return@launch

            titleAndLinkConsumer(title, "${urlDecider()}wiki/$title".toUri())
        }
    }

}