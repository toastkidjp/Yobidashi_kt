package jp.toastkid.web.archive

import android.content.Context
import android.webkit.WebView
import jp.toastkid.lib.storage.FilesDir

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
                makeNew(webView.context).assignNewFile(webView.title + fileExtension)
        )
    }

    /**
     * Supporting archive file extension.
     */
    private const val fileExtension: String = ".mht"

}
