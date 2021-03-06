package jp.toastkid.yobidashi.browser.archive

import android.view.View
import android.webkit.WebView
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import java.io.File

/**
 * Archive file saver.
 *
 * @author toastkidjp
 */
class ArchiveSaver {

    /**
     * Invoke saver.
     *
     * @param webView
     * @param file
     */
    operator fun invoke(
            webView: WebView,
            file: File
    ) {
        webView.saveWebArchive(file.absolutePath, false) {
                value -> saveToFile(webView, value)
        }
    }

    /**
     * Save archive content to file.
     *
     * @param view Snackbar's parent
     * @param value is nullable, because WebView#saveWebArchive is returnable null when it failed.
     */
    private fun saveToFile(view: View, value: String?) {
        val context = view.context
        val pair = PreferenceApplier(context).colorPair()
        if (value == null) {
            Toaster.snackShort(view, R.string.message_save_failed, pair)
            return
        }
        val message = context.getString(R.string.message_done_save) + value.substring(value.lastIndexOf("/"))
        Toaster.snackShort(view, message, pair)
    }

}
