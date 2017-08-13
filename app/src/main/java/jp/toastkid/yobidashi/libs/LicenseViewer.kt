package jp.toastkid.yobidashi.libs

import android.content.Context
import android.content.res.AssetManager
import android.support.v7.app.AlertDialog

import java.io.IOException
import java.io.InputStream
import java.util.LinkedHashMap

import jp.toastkid.yobidashi.R
import okio.Okio

/**
 * License files viewer.

 * @author toastkidjp
 */
class LicenseViewer
/**
 * Initialize with context.
 * @param context
 */
(
        /** For using get assets and show dialog.   */
        private val mContext: Context) {

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
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    private fun readUtf8(i: InputStream): String {
        return Okio.buffer(Okio.source(i)).readUtf8()
    }

    companion object {

        /** Directory name.  */
        private val DIRECTORY_OF_LICENSES = "licenses"
    }
}
