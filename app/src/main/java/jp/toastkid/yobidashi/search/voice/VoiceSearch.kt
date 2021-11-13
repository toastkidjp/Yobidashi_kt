package jp.toastkid.yobidashi.search.voice

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.view.View
import jp.toastkid.lib.intent.GooglePlayIntentFactory
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster

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
    fun makeIntent(context: Context): Intent =
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).also {
                it.putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
                )
                it.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }

    /**
     * Show Google App install suggestion by [com.google.android.material.snackbar.Snackbar].
     *
     * @param parent Snackbar parent
     * @param colorPair [ColorPair]
     */
    fun suggestInstallGoogleApp(parent: View, colorPair: ColorPair) {
        Toaster.withAction(
            parent,
            R.string.message_install_suggestion_google_app,
            R.string.install,
            { launchGooglePlay(parent) },
            colorPair
        )
    }

    /**
     * Launch Google Play App.
     *
     * @param parent Snackbar parent
     */
    private fun launchGooglePlay(parent: View) {
        parent.context.startActivity(makeGoogleAppInstallIntent())
    }

    /**
     * Make Google App [Intent].
     */
    private fun makeGoogleAppInstallIntent() =
        GooglePlayIntentFactory()("com.google.android.googlequicksearchbox")

    companion object {

    }
}
