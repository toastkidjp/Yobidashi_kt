package jp.toastkid.yobidashi.libs.storage

import android.content.Context

import java.io.File

/**
 * FilesDir's wrapper.

 * @author toastkidjp
 */
class Storeroom
/**
 * Initialize with context.

 * @param context
 *
 * @param dirName
 */
(context: Context, dirName: String) : StorageWrapper(context, dirName) {

    override fun getDir(context: Context): File {
        return context.filesDir
    }
}
