package jp.toastkid.jitte.browser.archive

import android.view.View
import android.webkit.WebView

import java.io.File

import jp.toastkid.jitte.R
import jp.toastkid.jitte.libs.Toaster
import jp.toastkid.jitte.libs.preference.PreferenceApplier

/**
 * Archive file saver.

 * @author toastkidjp
 */
internal object ArchiveSaver {

    /**
     * Invoke saver.
     * @param webView
     * *
     * @param file
     */
    operator fun invoke(
            webView: WebView,
            file: File
    ) {
        webView.saveWebArchive(
                file.absolutePath,
                false
        ) { value -> saveToFile(webView, value) }
    }

    /**
     * Save archive content to file.
     * @param view Snackbar's parent
     * *
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
