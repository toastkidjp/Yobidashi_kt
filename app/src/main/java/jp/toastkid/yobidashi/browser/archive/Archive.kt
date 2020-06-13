package jp.toastkid.yobidashi.browser.archive

import android.content.Context
import android.net.Uri
import android.os.Build
import android.webkit.WebView
import jp.toastkid.yobidashi.libs.storage.FilesDir
import java.io.File
import java.io.IOException

/**
 * Archive dir.
 *
 * @author toastkidjp
 */
internal object Archive {

    /**
     * Archive folder name.
     */
    private const val ARCHIVE_DIR = "archive"

    /**
     * Make new object.
     *
     * @param context
     *
     * @return [FilesDir] object.
     */
    fun makeNew(context: Context): FilesDir = FilesDir(context, ARCHIVE_DIR)

    /**
     * Save web archive.
     *
     * @param webView
     */
    fun save(webView: WebView) {
        ArchiveSaver().invoke(
                webView,
                makeNew(webView.context).assignNewFile(webView.title + Archive.fileExtension)
        )
    }

    /**
     * Supporting archive file extension.
     */
    private val fileExtension: String
        get() = if (canUseArchive()) ".mht" else ".xml"

    /**
     * Return can use archive.
     * @return If runtime environment SDK is Kitkat and upper, return false.
     */
    fun cannotUseArchive(): Boolean = !canUseArchive()

    /**
     * Return can use archive.
     * @return If runtime environment SDK is Kitkat and upper, return true.
     */
    private fun canUseArchive(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

}
