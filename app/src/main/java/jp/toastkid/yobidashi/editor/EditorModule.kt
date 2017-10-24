package jp.toastkid.yobidashi.editor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.view.View
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModuleEditorBinding
import jp.toastkid.yobidashi.libs.Colors
import jp.toastkid.yobidashi.libs.TextInputs
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.facade.BaseModule
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import okio.Okio
import java.io.File

/**
 * Editor activity.
 *
 * @param binding
 * @param intentLauncher
 * @param switchTabAction
 * @param closeTabAction
 *
 * @author toastkidjp
 */
class EditorModule(
        private val binding: ModuleEditorBinding,
        private val intentLauncher: (Intent, Int) -> Unit,
        private val switchTabAction: () -> Unit,
        private val closeTabAction: () -> Unit,
        private val saveTabCallback: (File) -> Unit
): BaseModule(binding.root) {

    /**
     * Use for clipping text.
     */
    private val cm: ClipboardManager
            = binding.root.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    /**
     * Preferences wrapper.
     */
    private val preferenceApplier: PreferenceApplier = PreferenceApplier(binding.root.context)

    /**
     * File path.
     */
    private var path: String = ""

    init {
        binding.save.setOnClickListener { save() }
        binding.load.setOnClickListener { load() }
        binding.clip.setOnClickListener { clip() }
        binding.tabList.setOnClickListener  { switchTabAction() }
        binding.close.setOnClickListener { hide() }
        Colors.setBgAndText(binding.save, preferenceApplier.colorPair())
        Colors.setBgAndText(binding.load, preferenceApplier.colorPair())
        Colors.setBgAndText(binding.clip, preferenceApplier.colorPair())
        Colors.setBgAndText(binding.tabList, preferenceApplier.colorPair())
        Colors.setBgAndText(binding.close, preferenceApplier.colorPair())
    }

    /**
     * Copy to clipboard current content.
     */
    private inline fun clip() {
        cm.primaryClip = ClipData.newPlainText("text", content())
    }

    /**
     * Return root view.
     *
     * @return [View]
     */
    fun view(): View = binding.root

    /**
     * Save current content to file.
     */
    fun save() {
        if (path.isNotEmpty()) {
            saveToFile()
            return
        }

        val context = binding.root.context
        val inputLayout = TextInputs.make(context)
        inputLayout.editText?.setText("memo")
        AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.title_dialog_input_file_name))
                .setView(inputLayout)
                .setCancelable(true)
                .setPositiveButton(R.string.save) { d, i ->
                    if (inputLayout.editText?.text?.isEmpty() as Boolean) {
                        return@setPositiveButton
                    }
                    val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    if (!externalFilesDir.exists()) {
                        externalFilesDir.mkdirs()
                    }
                    var newFile = File(
                            externalFilesDir,
                            "${inputLayout.editText?.text.toString()}.txt"
                    )
                    while (newFile.exists()) {
                        newFile = File(
                                externalFilesDir,
                                "${newFile.name.substring(0, newFile.name.lastIndexOf("."))}_.txt"
                        )
                    }
                    newFile.createNewFile()
                    path = newFile.absolutePath
                    saveTabCallback(newFile)
                    saveToFile()
                }
                .setNegativeButton(R.string.cancel) { d, i -> d.cancel() }
                .show()
    }

    /**
     * Save content to file.
     */
    private fun saveToFile() {
        Okio.buffer(Okio.sink(File(path))).write(contentBytes()).flush()
        MediaScannerConnection.scanFile(
                binding.root.context,
                arrayOf(path),
                null,
                { _, _ -> Toaster.snackShort(
                        binding.root,
                        "${context().getString(R.string.done_save)}: $path",
                        preferenceApplier.colorPair()
                ) })
    }

    /**
     * Load content from file with Storage Access Framework.
     */
    private inline fun load() {
        intentLauncher(makeStorageAccessIntent("text/plain"), REQUEST_CODE_LOAD)
    }

    /**
     * Make Storage Access Framework intent.
     *
     * @param type mime type
     * @return [Intent]
     */
    private inline fun makeStorageAccessIntent(type: String): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = type
        return intent
    }

    /**
     * Read content from file [Uri].
     *
     * @param data [Uri]
     */
    fun readFromFileUri(data: Uri) {
        extractFileFromUri(data)?.let { readFromFile(it) }
    }

    /**
     * Read content from [File].
     *
     * @param file [File]
     */
    fun readFromFile(file: File) {
        if (!file.exists() || !file.canRead()) {
            snackText(R.string.message_cannot_read_file)
            clearPath()
            return
        }
        val text = Okio.buffer(Okio.source(file)).readUtf8()
        binding.editorInput.setText(text)
        snackText(R.string.done_load)
        saveTabCallback(file)
    }

    /**
     * Clear current file path and reset edit-text.
     */
    fun clearPath() {
        path = ""
        binding.editorInput.setText("")
    }

    /**
     * Extract [File] object from [Uri]. This method is nullable.
     *
     * @param uri [Uri]
     * @return [File] (Nullable)
     */
    private inline fun extractFileFromUri(uri: Uri): File? {
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        val cursor = binding.root.context.contentResolver.query(uri, projection, null, null, null)
        return cursor?.let {
            var path: String? = null
            if (cursor.moveToFirst()) {
                path = cursor.getString(0)
            }
            cursor.close()
            return path?.let{ File(path) }
        }
    }

    /**
     * Return current content.
     *
     * @return content [String]
     */
    private inline fun content(): String = binding.editorInput.text.toString()

    /**
     * Return current content byte array.
     *
     * @return [ByteArray]
     */
    private inline fun contentBytes(): ByteArray = content().toByteArray()

    /**
     * Show snackbar with specified id text.
     *
     * @param id
     */
    private inline fun snackText(@StringRes id: Int) {
        Toaster.snackShort(binding.root, id, preferenceApplier.colorPair())
    }

    override fun show() {
        super.show()
        closeTabAction()
    }

    override fun hide() {
        super.hide()
        if (path.isNotEmpty()) {
            saveToFile()
        }
    }

    companion object {

        /**
         * Request code of specifying file.
         */
        const val REQUEST_CODE_LOAD: Int = 10111

    }

}