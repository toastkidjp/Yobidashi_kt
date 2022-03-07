/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.url_suggestion

import androidx.annotation.UiThread
import androidx.core.net.toUri
import jp.toastkid.lib.Urls
import jp.toastkid.yobidashi.browser.UrlItem
import jp.toastkid.yobidashi.browser.history.ViewHistory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RtsSuggestionUseCase(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    @UiThread
    suspend operator fun invoke(input: String?, itemCallback: (UrlItem) -> Unit) {
        val candidate = withContext(dispatcher) {
            if (Urls.isInvalidUrl(input)) {
                return@withContext null
            }

            val uri = input?.toUri() ?: return@withContext null
            if (uri.host?.endsWith("twitter.com") == true) {
                return@withContext uri.pathSegments[0]
            }

            return@withContext null
        }

        if (candidate.isNullOrBlank()) {
            return
        }

        itemCallback(
            ViewHistory().also {
                it.title = "$candidate をリアルタイム検索で見る"
                it.url = "https://search.yahoo.co.jp/realtime/search?p=id:$candidate"
            }
        )
    }
}