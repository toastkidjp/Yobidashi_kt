package jp.toastkid.yobidashi.libs.intent

import android.content.Intent

/**
 * Common [android.content.Intent] factory.
 *
 * @author toastkidjp
 */
object IntentFactory {

    /**
     * Make Storage Access Framework intent.
     *
     * @param type mime type
     * @return [Intent]
     */
    fun makeGetContent(type: String): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = type
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        return intent
    }

}
