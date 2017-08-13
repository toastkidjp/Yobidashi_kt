package jp.toastkid.yobidashi.libs.storage

import android.content.Context
import android.net.Uri

import java.io.File
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author toastkidjp
 */
internal abstract class StorageWrapper
/**
 * Initialize with context.
 * @param context
 */
(context: Context, dirName: String) {

    /** Directory object.  */
    private val dir: File

    init {
        dir = File(getDir(context), dirName)
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }

    protected abstract fun getDir(context: Context): File

    /**
     * Get file object.
     * @param index
     * *
     * @return
     */
    operator fun get(index: Int): File? {
        if (index < 0 || index > listFiles().size) {
            return null
        }
        return listFiles()[index]
    }

    fun remove(index: Int): Boolean {
        if (index < 0 || index > listFiles().size) {
            return false
        }
        return listFiles()[index].delete()
    }

    /**
     * Delete all files.
     */
    fun clean() {
        for (f in dir.listFiles()) {
            f.delete()
        }
    }

    /**
     * Get file count.
     * @return
     */
    val count: Int
        get() = listFiles().size

    /**
     * Assign new file.
     * @param uri
     * *
     * @return
     */
    fun assignNewFile(uri: Uri): File {
        return assignNewFile(File(uri.toString()).name)
    }

    /**
     * Assign new file.
     * @param name
     * *
     * @return
     */
    fun assignNewFile(name: String): File {
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
    private fun listFiles(): Array<File> {
        return dir.listFiles()
    }

    companion object {

        /** Illegal file name character.  */
        private val ILLEGAL_FILE_NAME_CHARACTER = Pattern.compile("[\\\\|/|:|\\*|\\?|\"|<|>|\\|]+", Pattern.DOTALL)
    }
}
