package jp.toastkid.yobidashi.libs.intent

import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.CalendarContract

/**
 * Common [android.content.Intent] factory.
 *
 * @author toastkidjp
 */
object IntentFactory {

    /**
     * Make pick image intent.
     * @return Intent
     */
    fun makePickImage(): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        return intent
    }

    /**
     * Make launching calendar intent.
     *
     * @return Intent
     */
    fun makeCalendar() = Intent(
            Intent.ACTION_VIEW,
            CalendarContract.CONTENT_URI.buildUpon().appendPath("time").build()
    )

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
     * Make author app intent.
     *
     * @return Intent of opening author apps.
     */
    fun authorsApp(): Intent =
            Intent(Intent.ACTION_VIEW)
                    .also { it.data = Uri.parse("market://search?q=pub:toastkidjp") }

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

    /**
     * Make Storage Access Framework intent.
     *
     * @param type mime type
     * @return [Intent]
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun makeOpenDocument(type: String): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = type
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        return intent
    }

}
