/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.main.launch

import android.net.Uri
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.wikipedia.random.RandomWikipedia

/**
 * @author toastkidjp
 */
class RandomWikipediaUseCase(
        private val contentViewModel: ContentViewModel?,
        private val openNewWebTab: (Uri) -> Unit,
        private val stringFinder: (Int, String) -> String
) {

    operator fun invoke() {
        RandomWikipedia().fetchWithAction { title, uri ->
            openNewWebTab(uri)
            contentViewModel?.snackShort(stringFinder(R.string.message_open_random_wikipedia, title))
        }
    }
}