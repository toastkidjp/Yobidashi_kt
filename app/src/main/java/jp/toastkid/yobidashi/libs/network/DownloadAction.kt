package jp.toastkid.yobidashi.libs.network

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.WifiConnectionChecker
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier


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
        if (PreferenceApplier(context).wifiOnly && WifiConnectionChecker.isNotConnecting(context)) {
            Toaster.tShort(context, R.string.message_wifi_not_connecting)
            return
        }
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)

        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        request.setVisibleInDownloadsUi(true)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, uri.lastPathSegment)
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
    }
}