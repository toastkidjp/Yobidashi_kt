package jp.toastkid.yobidashi.search.voice

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.view.View
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.search.SearchAction

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
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
                )
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }

    /**
     * Process activity result.
     *
     * @param context
     * @param data
     *
     * @return [Disposable]
     */
    fun processResult(context: Context, data: Intent): Disposable {
        val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (result.size == 0) {
            return Disposables.empty()
        }
        return SearchAction(context, PreferenceApplier(context).getDefaultSearchEngine(), result[0])
                .invoke()
    }

    /**
     * Show Google App install suggestion by [Snackbar].
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
