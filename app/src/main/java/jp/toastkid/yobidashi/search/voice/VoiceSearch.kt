package jp.toastkid.yobidashi.search.voice

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.view.View
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.search.SearchAction
import kotlinx.coroutines.Job

/**
 * Voice search use case.
 *
 * @author toastkidjp
 */
object VoiceSearch {

    /**
     * Request Code.
     */
    const val REQUEST_CODE: Int = 19001

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
     * Process activity result.
     *
     * @param context
     * @param data
     *
     * @return [Job]
     */
    fun processResult(context: Context, data: Intent): Job {
        val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (result.isNullOrEmpty()) {
            return Job()
        }
        return SearchAction(context, PreferenceApplier(context).getDefaultSearchEngine(), result[0])
                .invoke()
    }

    /**
     * Show Google App install suggestion by [android.support.design.widget.Snackbar].
     *
     * @param parent Snackbar's parent
     * @param colorPair [ColorPair]
     */
    fun suggestInstallGoogleApp(parent: View, colorPair: ColorPair) {
        Toaster.withAction(
                parent,
                R.string.message_install_suggestion_google_app,
                R.string.install,
                View.OnClickListener { launchGooglePlay(parent) },
                colorPair
        )
    }

    /**
     * Launch Google Play App.
     *
     * @param parent Snackbar's parent
     */
    private fun launchGooglePlay(parent: View) {
        parent.context.startActivity(makeGoogleAppInstallIntent())
    }

    /**
     * Make Google App [Intent].
     */
    private fun makeGoogleAppInstallIntent() =
            IntentFactory.googlePlay("com.google.android.googlequicksearchbox")
}
