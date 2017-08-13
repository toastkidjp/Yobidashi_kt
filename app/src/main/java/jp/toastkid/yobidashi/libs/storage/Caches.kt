package jp.toastkid.yobidashi.libs.storage

import android.content.Context

import java.io.File

/**
 * App's cache directory wrapper.

 * @author toastkidjp
 */
class Caches
/**
 * Initialize with context.

 * @param context
 * *
 * @param dirName
 */
(context: Context, dirName: String) : StorageWrapper(context, dirName) {

    override fun getDir(context: Context): File {
        return context.cacheDir
    }
}
