package jp.toastkid.yobidashi.libs

import android.annotation.TargetApi
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
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
    operator fun invoke(context: Context, uri: Uri): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && DocumentsContract.isDocumentUri(context, uri)) {
            return getForKitKat(context, uri)
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun getForKitKat(context: Context, uri: Uri): String? {
        when {
            "com.android.externalstorage.documents" == uri.authority -> {// ExternalStorageProvider
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                return if ("primary".equals(type, ignoreCase = true)) {
                    Environment.getExternalStorageDirectory().resolve(split[1]).absolutePath
                } else if ("home".equals(type, ignoreCase = true)) {
                    Environment.getExternalStorageDirectory().resolve("documents/${split[1]}").absolutePath
                } else {
                    "/storage/${type}/${split[1]}"
                }
            }
            "com.android.providers.downloads.documents" == uri.authority -> {// DownloadsProvider
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)!!)
                return getDataColumn(context, contentUri, null, null)
            }
            "com.android.providers.media.documents" == uri.authority -> {// MediaProvider
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                var contentUri: Uri? = null
                contentUri = MediaStore.Files.getContentUri("external")
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
            else -> return ""
        }
    }

    private fun getDataColumn(
            context: Context,
            uri: Uri?,
            selection: String?,
            selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
        try {
            cursor = context.contentResolver.query(
                    uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(projection[0])
                return cursor.getString(columnIndex)
            }
        } finally {
            cursor?.close()
        }
        return null
    }
}