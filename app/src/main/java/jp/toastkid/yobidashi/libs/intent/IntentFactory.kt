package jp.toastkid.yobidashi.libs.intent

import android.content.Intent
import android.net.Uri

/**
 * Common [android.content.Intent] factory.
 *
 * @author toastkidjp
 */
object IntentFactory {

    /**
     * Make launching Google Play intent.
     *
     * @param packageName
     * @return Google play intent.
     */
    fun googlePlay(packageName: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://details?id=" + packageName)
        return intent
    }

    /**
     * Share image uri.
     *
     * @param uri
     * @return
     */
    fun shareImage(uri: Uri): Intent {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(uri, "image/*")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        return intent
    }

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
