package jp.toastkid.lib.storage

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.regex.Pattern

/**
 * Initialize with context.
 *
 * @param context
 * @param dirName
 * @author toastkidjp
 */
sealed class StorageWrapperImplementation(context: Context, dirName: String) : StorageWrapper {

    /**
     * Directory object.
     */
    private val dir = File(getDir(context), dirName)

    init {
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }

    /**
     * Get file object.
     * @param index
     *
     * @return File object
     */
    override operator fun get(index: Int): File? {
        if (index < 0 || index > listFiles().size) {
            return null
        }
        return listFiles()[index]
    }

    /**
     * Remove item which specified position.
     *
     * @param index
     */
    override fun removeAt(index: Int): Boolean {
        if (index < 0 || index > listFiles().size) {
            return false
        }
        return listFiles()[index].delete()
    }

    override fun findByName(name: String): File? = listFiles().firstOrNull { it.name.equals(name) }

    override fun delete(name: String) {
        assignNewFile(name).delete()
    }

    /**
     * Delete all files.
     */
    override fun clean() = dir.listFiles().forEach { it.delete() }

    /**
     * Assign new file.
     * @param uri
     *
     * @return [File]
     */
    override fun assignNewFile(uri: Uri): File = assignNewFile(File(uri.toString()).name)

    /**
     * Assign new file.
     * @param name
     *
     * @return [File]
     */
    override fun assignNewFile(name: String): File {
        val matcher = ILLEGAL_FILE_NAME_CHARACTER.matcher(name)
        if (matcher.find()) {
            return File(dir, matcher.replaceAll("_"))
        }
        return File(dir, name)
    }

    /**
     * Internal method.
     * @return
     */
    override fun listFiles(): Array<File> = dir.listFiles()

    /**
     * Get file count.
     * @return file count
     */
    val count: Int
        get() = listFiles().size

    companion object {

        /** Illegal file name character.  */
        private val ILLEGAL_FILE_NAME_CHARACTER = Pattern.compile("[\\\\|/|:|\\*|\\?|\"|<|>|\\|]+", Pattern.DOTALL)
    }
}

/**
 * App's cache directory wrapper.
 *
 * @param context
 * @param dirName
 * @author toastkidjp
 */
class CacheDir(context: Context, dirName: String) : StorageWrapperImplementation(context, dirName) {

    override fun getDir(context: Context): File = context.cacheDir
}

/**
 * FilesDir's wrapper.
 *
 * @param context
 * @param dirName
 *
 * @author toastkidjp
 */
class FilesDir(context: Context, dirName: String) : StorageWrapperImplementation(context, dirName) {

    override fun getDir(context: Context): File = context.filesDir
}
