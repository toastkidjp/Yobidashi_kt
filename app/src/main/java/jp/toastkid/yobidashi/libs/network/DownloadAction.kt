package jp.toastkid.yobidashi.libs.network

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import jp.toastkid.lib.permission.RuntimePermissions
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


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
        if (PreferenceApplier(context).wifiOnly && WifiConnectionChecker.isNotConnecting(context)) {
            Toaster.tShort(context, R.string.message_wifi_not_connecting)
            return
        }

        val activity = context as? FragmentActivity ?: return

        CoroutineScope(Dispatchers.Main).launch {
            RuntimePermissions(activity)
                    .request(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    ?.receiveAsFlow()
                    ?.collect { permission ->
                        if (!permission.granted) {
                            Toaster.tShort(activity, R.string.message_requires_permission_storage)
                            return@collect
                        }
                        enqueue(urls)
                    }
        }
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
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, uri.lastPathSegment)
        request.setAllowedOverMetered(false)
        request.setAllowedOverRoaming(false)
        return request
    }
}