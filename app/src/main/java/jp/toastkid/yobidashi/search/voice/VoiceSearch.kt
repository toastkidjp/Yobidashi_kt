package jp.toastkid.yobidashi.search.voice

import android.content.Intent
import android.speech.RecognizerIntent
import jp.toastkid.lib.intent.GooglePlayIntentFactory
import jp.toastkid.yobidashi.BuildConfig

/**
 * Voice search use case.
 *
 * @author toastkidjp
 */
class VoiceSearch {

    /**
     * Make intent.
     *
     * @param context
     * @return [Intent]
     */
    fun makeIntent(): Intent =
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).also {
                it.putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
                )
                it.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, BuildConfig.APPLICATION_ID)
            }

    /**
     * Make Google App [Intent].
     */
    private fun makeGoogleAppInstallIntent() =
        GooglePlayIntentFactory()("com.google.android.googlequicksearchbox")

}
