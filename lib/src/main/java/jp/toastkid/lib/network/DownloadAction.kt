package jp.toastkid.lib.network

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri


/**
 * Method object of downloading.
 *
 * @param context [Context]
 * @author toastkidjp
 */
class DownloadAction(private val context: Context) {

    operator fun invoke(url: String) {
        invoke(listOf(url))
    }

    operator fun invoke(urls: Collection<String>) {
        enqueue(urls)
    }

    private fun enqueue(urls: Collection<String>) {
        val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (externalFilesDir?.exists() == false) {
            externalFilesDir.mkdirs()
        }

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        urls.map { makeRequest(it.toUri(), urls.size == 1) }.forEach { dm?.enqueue(it) }
    }

    private fun makeRequest(uri: Uri, showComplete: Boolean): DownloadManager.Request {
        val request = DownloadManager.Request(uri)
        request.setNotificationVisibility(
                if (showComplete) DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                else DownloadManager.Request.VISIBILITY_VISIBLE
        )
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            uri.lastPathSegment
        )
        request.setAllowedOverMetered(false)
        request.setAllowedOverRoaming(false)
        return request
    }
}