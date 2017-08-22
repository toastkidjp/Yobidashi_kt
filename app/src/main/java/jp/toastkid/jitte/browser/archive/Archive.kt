package jp.toastkid.jitte.browser.archive

import android.content.Context
import android.net.Uri
import android.os.Build
import android.webkit.WebView
import jp.toastkid.jitte.libs.storage.Storeroom
import java.io.File
import java.io.IOException

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
    fun makeNew(context: Context): Storeroom {
        return Storeroom(context, ARCHIVE_DIR)
    }

    /**
     * Save web archive.
     * @param webView
     */
    fun save(webView: WebView) {
        ArchiveSaver.invoke(
                webView,
                makeNew(webView.context).assignNewFile(webView.title + Archive.fileExtension)
        )
    }

    private val fileExtension: String
        get() = if (canUseArchive()) ".mht" else ".xml"

    fun cannotUseArchive(): Boolean {
        return !canUseArchive()
    }

    fun canUseArchive(): Boolean {
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
