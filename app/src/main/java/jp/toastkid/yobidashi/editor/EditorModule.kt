package jp.toastkid.yobidashi.editor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.View
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModuleEditorBinding
import jp.toastkid.yobidashi.libs.Colors
import jp.toastkid.yobidashi.libs.FileExtractorFromUri
import jp.toastkid.yobidashi.libs.TextInputs
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.facade.BaseModule
import jp.toastkid.yobidashi.libs.intent.IntentFactory
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
 * @param saveTabCallback
 * @param toolbarCallback
 *
 * @author toastkidjp
 */
class EditorModule(
        private val binding: ModuleEditorBinding,
        private val intentLauncher: (Intent, Int) -> Unit,
        private val switchTabAction: () -> Unit,
        private val closeTabAction: () -> Unit,
        private val saveTabCallback: (File) -> Unit,
        private val toolbarCallback: (Boolean) -> Unit
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
        val context = binding.root.context

        binding.save.setOnClickListener { save() }
        binding.load.setOnClickListener { load() }
        binding.clip.setOnClickListener { clip() }
        binding.tabList.setOnClickListener { switchTabAction() }
        binding.count.setOnClickListener {
            Toaster.tShort(
                    context,
                    context.getString(R.string.message_character_count, content().length)
            )
        }
        binding.backup.setOnClickListener { backup() }
        binding.toTop.setOnClickListener { top() }
        binding.toBottom.setOnClickListener { bottom() }
        binding.clear.setOnClickListener {
            AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.title_clear_text))
                    .setMessage(Html.fromHtml(context.getString(R.string.confirm_clear_all_settings)))
                    .setNegativeButton(R.string.cancel, {d, i -> d.cancel()})
                    .setPositiveButton(R.string.ok, {d, i ->
                        clearInput()
                        d.dismiss()
                    })
                    .show()
        }
        val colorPair = preferenceApplier.colorPair()
        Colors.setBgAndText(binding.save, colorPair)
        Colors.setBgAndText(binding.load, colorPair)
        Colors.setBgAndText(binding.clip, colorPair)
        Colors.setBgAndText(binding.tabList, colorPair)
        Colors.setBgAndText(binding.count, colorPair)
        Colors.setBgAndText(binding.backup, colorPair)
        Colors.setBgAndText(binding.toTop, colorPair)
        Colors.setBgAndText(binding.toBottom, colorPair)
        Colors.setBgAndText(binding.clear, colorPair)
    }

    /**
     * Backup current file.
     */
    private inline fun backup() {
        if (path.isEmpty()) {
            save()
            return
        }
        val fileName = removeExtension(File(path).name) + "_backup.txt"
        saveToFile(assignFile(binding.root.context, fileName).absolutePath)
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
            saveToFile(path)
            return
        }

        val context = binding.root.context
        val inputLayout = TextInputs.make(context)
        inputLayout.editText?.setText(DEFAULT_FILE_NAME)
        AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.title_dialog_input_file_name))
                .setView(inputLayout)
                .setCancelable(true)
                .setPositiveButton(R.string.save) { d, i ->
                    if (inputLayout.editText?.text?.isEmpty() as Boolean) {
                        return@setPositiveButton
                    }

                    var newFile = assignFile(context, "${inputLayout.editText?.text.toString()}.txt")
                    while (newFile.exists()) {
                        newFile = assignFile(
                                context,
                                "${removeExtension(newFile.name)}_.txt"
                        )
                    }
                    path = newFile.absolutePath
                    saveTabCallback(newFile)
                    saveToFile(path)
                }
                .setNegativeButton(R.string.cancel) { d, i -> d.cancel() }
                .show()
    }

    /**
     * Call from fragment's onPause().
     */
    fun saveIfNeed() {
        if (path.isNotEmpty()) {
            saveToFile(path)
        }
    }

    /**
     * Remove extension from passed text.
     *
     * @param fileName
     * @return string
     */
    private inline fun removeExtension(fileName: String): String {
        val endIndex = fileName.lastIndexOf(".")
        return if (endIndex == -1) fileName else fileName.substring(0, endIndex)
    }

    /**
     * Assign file in environment download directory.
     *
     * @param context
     * @param fileName
     * @return [File]
     */
    private inline fun assignFile(context: Context, fileName: String): File {
        val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (!externalFilesDir.exists()) {
            externalFilesDir.mkdirs()
        }
        return File(externalFilesDir, fileName)
    }

    /**
     * Save content to file.
     *
     * @param filePath
     */
    private fun saveToFile(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            file.createNewFile()
        }
        Okio.buffer(Okio.sink(file)).write(contentBytes()).flush()
        MediaScannerConnection.scanFile(
                binding.root.context,
                arrayOf(filePath),
                null,
                { _, _ -> Toaster.snackShort(
                        binding.root,
                        "${context().getString(R.string.done_save)}: $filePath",
                        preferenceApplier.colorPair()
                ) })
    }

    /**
     * Go to top.
     */
    private inline fun top() {
        binding.editorInput.setSelection(0)
    }

    /**
     * Go to bottom.
     */
    private inline fun bottom() {
        binding.editorInput.setSelection(binding.editorInput.text.length)
    }

    /**
     * Load content from file with Storage Access Framework.
     */
    private inline fun load() {
        intentLauncher(IntentFactory.makeStorageAccess("text/plain"), REQUEST_CODE_LOAD)
    }

    /**
     * Read content from file [Uri].
     *
     * @param data [Uri]
     */
    fun readFromFileUri(data: Uri) {
        FileExtractorFromUri(binding.root.context, data)?.let { readFromFile(it) }
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
        path = file.absolutePath
        saveTabCallback(file)
    }

    /**
     * Clear current file path and reset edit-text.
     */
    fun clearPath() {
        path = ""
        clearInput()
    }

    /**
     * Clear input text.
     */
    private inline fun clearInput() {
        binding.editorInput.setText("")
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
        toolbarCallback(true)
    }

    override fun hide() {
        super.hide()
        if (path.isNotEmpty()) {
            saveToFile(path)
        }
        toolbarCallback(false)
    }

    companion object {

        /**
         * Request code of specifying file.
         */
        const val REQUEST_CODE_LOAD: Int = 10111

        /**
         * Default file name.
         */
        private const val DEFAULT_FILE_NAME: String = "memo"
    }

}