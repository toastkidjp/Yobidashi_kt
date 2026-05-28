/*
 * Copyright (c) 2026 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.search.voice

import android.content.Intent
import android.speech.RecognizerIntent

/**
 * Voice search use case.
 *
 * @author toastkidjp
 */
class VoiceSearchIntentFactory {

    /**
     * Make intent.
     *
     * TODO Hard coded.
     *
     * @param context
     * @return [Intent]
     */
    operator fun invoke(): Intent =
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).also {
                it.putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
                )
                it.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "jp.toastkid.yobidashi")
            }

}
