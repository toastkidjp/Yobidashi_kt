package jp.toastkid.yobidashi.search.voice

import android.content.Intent
import android.speech.RecognizerIntent
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.intent.GooglePlayIntentFactory
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.R

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
     * Show Google App install suggestion by [com.google.android.material.snackbar.Snackbar].
     *
     * @param parent Snackbar parent
     * @param colorPair [ColorPair]
     */
    fun suggestInstallGoogleApp(parent: View) {
        val contentViewModel = (parent.context as? ViewModelStoreOwner)?.let {
            ViewModelProvider(it).get(ContentViewModel::class.java)
        }
        contentViewModel?.snackWithAction(
            parent.context.getString(R.string.message_install_suggestion_google_app),
            parent.context.getString(R.string.install),
            { launchGooglePlay(parent) }
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

}
