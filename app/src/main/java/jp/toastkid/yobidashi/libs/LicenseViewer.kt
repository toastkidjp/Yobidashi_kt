package jp.toastkid.yobidashi.libs

import android.content.Context
import android.support.v7.app.AlertDialog
import jp.toastkid.yobidashi.R
import okio.Okio
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * License files viewer.
 *
 * Initialize with context.
 * @param context For using makeNew assets and show dialog.
 *
 * @author toastkidjp
 */
class LicenseViewer(private val mContext: Context) {

    /**
     * Invoke viewer.
     */
    operator fun invoke() {
        try {
            val assets = mContext.assets
            val licenseFiles = assets.list(DIRECTORY_OF_LICENSES)
            val licenseMap = LinkedHashMap<String, String>(licenseFiles.size)
            for (fileName in licenseFiles) {
                val stream = assets.open(DIRECTORY_OF_LICENSES + "/" + fileName)
                licenseMap.put(fileName.substring(0, fileName.lastIndexOf(".")), readUtf8(stream))
                stream.close()
            }
            val items = licenseMap.keys.toTypedArray()
            AlertDialog.Builder(mContext).setTitle(R.string.title_licenses)
                    .setItems(items
                    ) { d, index ->
                        AlertDialog.Builder(mContext)
                                .setTitle(items[index])
                                .setMessage(licenseMap[items[index]])
                                .setCancelable(true)
                                .setPositiveButton(R.string.close) { dialog, i -> dialog.dismiss() }
                                .show()
                    }
                    .setCancelable(true)
                    .setPositiveButton(R.string.close) { d, i -> d.dismiss() }
                    .show()
        } catch (e: IOException) {
            Timber.e(e)
        }

    }

    /**
     * Read string from passed [InputStream].
     *
     * @param inputStream [InputStream]]
     */
    @Throws(IOException::class)
    private fun readUtf8(inputStream: InputStream): String
            = Okio.buffer(Okio.source(inputStream)).readUtf8()

    companion object {

        /**
         * Directory name.
         */
        private val DIRECTORY_OF_LICENSES = "licenses"
    }
}
