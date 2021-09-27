/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.editor.usecase

import androidx.annotation.WorkerThread
import org.jsoup.Jsoup
import java.net.URL

class LinkTitleFetcherUseCase {

    @WorkerThread
    operator fun invoke(url: String): String {
        return Jsoup.parse(URL(url), 1000).title() ?: url
    }

}