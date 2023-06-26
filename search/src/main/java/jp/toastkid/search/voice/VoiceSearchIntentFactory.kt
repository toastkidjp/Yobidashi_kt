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
