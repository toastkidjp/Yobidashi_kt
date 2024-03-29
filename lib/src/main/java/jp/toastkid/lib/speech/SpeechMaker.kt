/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.libs.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.UUID
import kotlin.math.min

/**
 * @author toastkidjp
 */
class SpeechMaker(
    context: Context,
    private val textToSpeech: TextToSpeech = TextToSpeech(context) {  }
) {

    operator fun invoke(message: String) {
        if (textToSpeech.isSpeaking) {
            stop()
            return
        }

        textToSpeech.speak(
                message.substring(0, min(message.length, 3999)),
                TextToSpeech.QUEUE_FLUSH,
                null,
                UUID.randomUUID().toString()
        )
    }

    fun stop() {
        textToSpeech.stop()
    }

    fun dispose() {
        stop()
        textToSpeech.shutdown()
    }
}