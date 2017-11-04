package jp.toastkid.yobidashi.libs

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.File

/**
 * File object extractor from [Uri].
 *
 * @author toastkidjp
 */
object FileExtractorFromUri {

    /**
     * Extract [File] object from [Uri]. This method is nullable.
     *
     * @param context [Context]
     * @param uri [Uri]
     * @return [File] (Nullable)
     */
    operator fun invoke(context: Context, uri: Uri): File? {
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        return cursor?.let {
            var path: String? = null
            if (cursor.moveToFirst()) {
                path = cursor.getString(0)
            }
            cursor.close()
            return path?.let{ File(path) }
        }
    }
}