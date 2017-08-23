package jp.toastkid.jitte.libs.network

import android.app.DownloadManager
import android.content.Context
import android.net.Uri

/**
 * Method object of downloading.
 *
 * @author toastkidjp
 */
class DownloadAction(
        val context: Context,
        val url: String
) {
    fun invoke() {
        val request = DownloadManager.Request(Uri.parse(url))

        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setVisibleInDownloadsUi(true)
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
    }
}