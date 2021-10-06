/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.intent

import android.content.Intent

class ShareIntentFactory {

    /**
     * Make sharing message intent.
     *
     * @param message
     * @return Intent
     */
    operator fun invoke(message: String, subject: String? = null): Intent {
        val intent = Intent().also {
            it.action = Intent.ACTION_SEND
            it.type = "text/plain"
            it.putExtra(Intent.EXTRA_TEXT, message)
            subject?.also { subject ->
                it.putExtra(Intent.EXTRA_SUBJECT, subject);
            }
        }
        return Intent.createChooser(intent, "Select app for share")
    }

}