package jp.toastkid.yobidashi.libs.network

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.permission.RuntimePermissions
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


/**
 * Method object of downloading.
 *
 * @author toastkidjp
 */
class DownloadAction(
        val context: Context,
        val url: String
) {
    operator fun invoke() {
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
                        enqueue()
                    }
        }
    }

    private fun enqueue() {
        val uri = url.toUri()
        val request = DownloadManager.Request(uri)

        request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, uri.lastPathSegment)

        val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (externalFilesDir?.exists() == false) {
            externalFilesDir.mkdirs()
        }

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        dm?.enqueue(request)
    }
}