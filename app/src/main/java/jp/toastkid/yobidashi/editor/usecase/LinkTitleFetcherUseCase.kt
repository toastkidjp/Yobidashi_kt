/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.editor.usecase

import androidx.annotation.WorkerThread
import jp.toastkid.yobidashi.libs.network.HttpClientFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.regex.Pattern

class LinkTitleFetcherUseCase(
    private val okHttpClient: OkHttpClient = HttpClientFactory.withTimeout(5)
) {

    @WorkerThread
    operator fun invoke(url: String): String {
        val content = okHttpClient.newCall(Request.Builder().url(url).build())
            .execute()
            .body
            ?.string()
            ?.split(System.lineSeparator())
            ?.firstOrNull { it.contains("<title") } ?: return url

        val matcher = PATTERN.matcher(content)
        return if (matcher.find()) "[${matcher.group(1)}]($url)" else url
    }

    companion object {

        private val PATTERN = Pattern.compile(
            "<title.*>(.+?)</title>",
            Pattern.DOTALL or Pattern.CASE_INSENSITIVE
        )

    }

}