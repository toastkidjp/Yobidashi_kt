package jp.toastkid.yobidashi.search.voice

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.search.SearchAction

/**
 * Voice search use case.

 * @author toastkidjp
 */
object VoiceSearch {

    /**
     * Make intent.

     * @param context
     *
     * @return
     */
    fun makeIntent(context: Context): Intent {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
        )
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        return intent
    }

    /**
     * Process activity result.

     * @param context
     *
     * @param data
     */
    fun processResult(context: Context, data: Intent): Disposable {
        val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (result.size == 0) {
            return Disposables.empty()
        }
        return SearchAction(context, PreferenceApplier(context).getDefaultSearchEngine(), result[0])
                .invoke()
    }
}
