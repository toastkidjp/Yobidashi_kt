package jp.toastkid.yobidashi.editor

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.annotation.MainThread
import android.support.annotation.StringRes
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.animation.Animation
import android.widget.TextView
import com.cleveroad.cyclemenuwidget.CycleMenuWidget
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModuleEditorBinding
import jp.toastkid.yobidashi.libs.FileExtractorFromUri
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.facade.BaseModule
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import okio.Okio
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Editor activity.
 *
 * @param binding
 * @param intentLauncher
 * @param saveTabCallback
 *
 * @author toastkidjp
 */
class EditorModule(
        private val binding: ModuleEditorBinding,
        private val intentLauncher: (Intent, Int) -> Unit,
        private val saveTabCallback: (File) -> Unit
): BaseModule(binding.root) {

    /**
     * Preferences wrapper.
     */
    private val preferenceApplier: PreferenceApplier = PreferenceApplier(binding.root.context)

    /**
     * Default date format holder.
     */
    private val dateFormatHolder: ThreadLocal<DateFormat> by lazy {
        object: ThreadLocal<DateFormat>() {
            override fun initialValue(): DateFormat =
                    SimpleDateFormat(binding.root.context.getString(R.string.date_format), Locale.getDefault())
        }
    }

    /**
     * File path.
     */
    private var path: String = ""

    init {
        val context = binding.root.context

        binding.save.setOnClickListener { save() }
        binding.saveAs.setOnClickListener { saveAs() }
        binding.load.setOnClickListener { load() }
        binding.backup.setOnClickListener { backup() }
        binding.pasteAsQuotation.setOnClickListener { pasteAsQuotation() }
        binding.clear.setOnClickListener {
            ClearTextDialogFragment.show(context)
        }

        binding.editorInput.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(contentEditable: Editable?) {
                setContentTextLengthCount(context)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

        })
    }

    /**
     * Apply color setting.
     */
    fun applyColor() {
        val colorPair = preferenceApplier.colorPair()
        applyButtonColor(
                colorPair,
                binding.save,
                binding.saveAs,
                binding.load,
                binding.lastSaved,
                binding.counter,
                binding.backup,
                binding.pasteAsQuotation,
                binding.clear
                )

        binding.editorMenu.setBackgroundColor(colorPair.bgColor())
    }

    /**
     * Apply button color to multiple [TextView].
     *
     * @param colorPair [ColorPair]
     * @param textViews multiple [TextView]
     */
    private fun applyButtonColor(colorPair: ColorPair, vararg textViews: TextView) {
        val fontColor = colorPair.fontColor()
        textViews.forEach { textView ->
            textView.setTextColor(fontColor)
            textView.compoundDrawables?.forEach {
                it?.colorFilter = PorterDuffColorFilter(fontColor, PorterDuff.Mode.SRC_IN)
            }
        }
    }

    /**
     * Set content text length count to binding.counter.
     *
     * @param context Context
     */
    private fun setContentTextLengthCount(context: Context) {
        binding.counter.text =
                context.getString(R.string.message_character_count, content().length)
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
     * Paste clipped text as Markdown's quotation style.
     */
    private fun pasteAsQuotation() {
        val primary = Clipboard.getPrimary(context())
        if (TextUtils.isEmpty(primary)) {
            return
        }
        binding.editorInput.text.insert(binding.editorInput.selectionStart, Quotation(primary))
    }

    /**
     * Go to top.
     */
    fun pageUp() {
        binding.editorInput.setSelection(0)
    }

    /**
     * Go to bottom.
     */
    fun pageDown() {
        binding.editorInput.setSelection(binding.editorInput.length())
    }

    /**
     * Set space for showing [CycleMenuWidget].
     *
     * @param menuPos [CycleMenuWidget.CORNER]
     */
    fun setSpace(menuPos: CycleMenuWidget.CORNER) = when (menuPos) {
        CycleMenuWidget.CORNER.LEFT_BOTTOM -> {
            binding.leftSpace.visibility = View.VISIBLE
            binding.rightSpace.visibility = View.GONE
        }
        CycleMenuWidget.CORNER.RIGHT_BOTTOM -> {
            binding.leftSpace.visibility = View.GONE
            binding.rightSpace.visibility = View.VISIBLE
        }
        else -> Unit
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

        InputNameDialogFragment.show(context())
    }

    /**
     * Save current text as other file.
     */
    private fun saveAs() {
        InputNameDialogFragment.show(context())
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
        val externalFilesDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        } else {
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        }

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
        Okio.buffer(Okio.sink(file)).use {
            it.writeUtf8(content())
            it.flush()
        }
        val context = binding.root.context
        MediaScannerConnection.scanFile(
                context,
                arrayOf(filePath),
                null,
                { _, _ ->  })
        snackText("${context().getString(R.string.done_save)}: $filePath")
        setLastSaved(file.lastModified())
    }

    /**
     * Load content from file with Storage Access Framework.
     */
    private inline fun load() {
        intentLauncher(IntentFactory.makeGetContent("text/plain"), REQUEST_CODE_LOAD)
    }

    /**
     * Read content from file [Uri].
     *
     * @param data [Uri]
     */
    fun readFromFileUri(data: Uri) {
        FileExtractorFromUri(binding.root.context, data)?.let {
            readFromFile(File(it))
        }
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
        val text = Okio.buffer(Okio.source(file)).use { it.readUtf8() }
        setContentText(text)
        snackText(R.string.done_load)
        path = file.absolutePath
        saveTabCallback(file)

        setLastSaved(file.lastModified())
    }

    /**
     * Set last modified time.
     *
     * @param ms
     */
    private fun setLastSaved(ms: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = ms
        binding.lastSaved.setText("Last saved:\n" + dateFormatHolder.get().format(calendar.time))
    }

    /**
     * Clear current file path and reset edit-text.
     */
    fun clearPath() {
        path = ""
        clearInput()
        binding.lastSaved.setText("")
    }

    /**
     * Clear input text.
     */
    fun clearInput() {
        setContentText("")
    }

    /**
     * Set content string and set text length.
     */
    private fun setContentText(contentStr: String) {
        binding.editorInput.setText(contentStr)
        setContentTextLengthCount(binding.root.context)
    }

    /**
     * Return current content.
     *
     * @return content [String]
     */
    private inline fun content(): String = binding.editorInput.text.toString()

    /**
     * Show snackbar with specified id text.
     *
     * @param id
     */
    private inline fun snackText(@StringRes id: Int) {
        Toaster.snackShort(binding.root, id, preferenceApplier.colorPair())
    }

    /**
     * Animate root view with specified [Animation].
     *
     * @param animation
     */
    fun animate(animation: Animation) {
        view().startAnimation(animation)
    }

    /**
     * Make thumbnail.
     *
     * @return Bitmap or null
     */
    @MainThread
    fun makeThumbnail(): Bitmap? = binding.root.run {
        invalidate()
        buildDrawingCache()
        return drawingCache
    }

    /**
     * Assign new file object.
     *
     * @param fileName
     */
    fun assignNewFile(fileName: String) {
        val context = context()
        var newFile = assignFile(context, fileName)
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

    /**
     * Show message by [Snackbar].
     *
     * @param message
     */
    private fun snackText(message: String) {
        Toaster.snackShort(
                binding.root,
                message,
                preferenceApplier.colorPair()
        )
    }

    override fun hide() {
        super.hide()
        if (path.isNotEmpty()) {
            saveToFile(path)
        }
    }

    companion object {

        /**
         * Request code of specifying file.
         */
        const val REQUEST_CODE_LOAD: Int = 10111

    }

}