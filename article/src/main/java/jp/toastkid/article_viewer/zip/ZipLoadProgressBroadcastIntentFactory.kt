/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.zip

import android.content.Intent
import android.content.IntentFilter

class ZipLoadProgressBroadcastIntentFactory {

    operator fun invoke(progress: Int): Intent {
        val progressIntent = Intent(ACTION_PROGRESS_BROADCAST)
        progressIntent.putExtra("progress", progress)
        return progressIntent
    }

    companion object {

        private const val ACTION_PROGRESS_BROADCAST = "jp.toastkid.articles.importing.progress"

        fun makeProgressBroadcastIntentFilter() = IntentFilter(ACTION_PROGRESS_BROADCAST)

    }
}