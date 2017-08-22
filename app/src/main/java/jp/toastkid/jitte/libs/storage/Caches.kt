package jp.toastkid.jitte.libs.storage

import android.content.Context

import java.io.File

/**
 * App's cache directory wrapper.

 * @author toastkidjp
 */
internal class Caches
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
