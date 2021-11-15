/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.intent

import android.content.Intent

class UrlShareIntentFactory {

    /**
     * Make sharing URL intent.
     *
     * @param url URL
     */
    operator fun invoke(url: String): Intent {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        share.putExtra(Intent.EXTRA_SUBJECT, "Share link")
        share.putExtra(Intent.EXTRA_TEXT, url)
        return Intent.createChooser(share, "Share link $url")
    }

}