package jp.toastkid.yobidashi.browser.archive

import android.content.Context
import android.net.Uri
import android.os.Build
import android.webkit.WebView

import java.io.File
import java.io.IOException

import jp.toastkid.yobidashi.libs.storage.Storeroom

/**
 * Archive dir.

 * @author toastkidjp
 */
object Archive {

    /** Archive folder name.  */
    private val ARCHIVE_DIR = "archive"

    /**
     * Make new object.

     * @param context
     * *
     * @return
     */
    operator fun get(context: Context): Storeroom {
        return Storeroom(context, ARCHIVE_DIR)
    }

    /**
     * Save web archive.
     * @param webView
     */
    fun save(webView: WebView) {
        ArchiveSaver.invoke(
                webView,
                get(webView.context).assignNewFile(webView.title + Archive.fileExtension)
        )
    }

    private val fileExtension: String
        get() = if (canUseArchive()) ".mht" else ".xml"

    fun cannotUseArchive(): Boolean {
        return !canUseArchive()
    }

    private fun canUseArchive(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
    }

    /**
     * Load archive.

     * @param webView
     * *
     * @param file
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun loadArchive(webView: WebView, file: File) {
        webView.loadUrl(Uri.fromFile(file).toString())
    }
}
