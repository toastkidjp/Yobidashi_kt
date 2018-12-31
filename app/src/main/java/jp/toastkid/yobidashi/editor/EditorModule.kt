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
import android.support.annotation.Dimension
import android.support.annotation.MainThread
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
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
 * Editor module.
 *
 * @param binding [ModuleEditorBinding]
 * @param intentLauncher Intent launcher for using load function
 * @param saveTabCallback Callback of tab saving
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
                    SimpleDateFormat(binding.root.context.getString(R.string.editor_date_format), Locale.getDefault())
        }
    }

    /**
     * Last saved text.
     */
    private var lastSavedTitle: String

    /**
     * File path.
     */
    private var path: String = ""

    /**
     * Last index of find-text.
     */
    private var lastIndex = 0

    init {
        val context = binding.root.context

        binding.editorModule = this

        binding.editorInput.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(contentEditable: Editable?) {
                setContentTextLengthCount(context)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

        })
        lastSavedTitle = context().getString(R.string.last_saved)
    }

    /**
     * Apply color and font setting.
     */
    fun applySettings() {
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

        binding.background.setBackgroundColor(preferenceApplier.editorBackgroundColor())
        binding.editorInput.setTextColor(preferenceApplier.editorFontColor())
        binding.editorInput.setTextSize(Dimension.SP, preferenceApplier.editorFontSize().toFloat())
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
    fun backup() {
        if (path.isEmpty()) {
            save()
            return
        }
        val fileName = removeExtension(File(path).name) + "_backup.txt"
        saveToFile(assignFile(binding.root.context, fileName).absolutePath)
    }

    fun clear() {
        ClearTextDialogFragment.show(binding.root.context)
    }

    /**
     * Paste clipped text as Markdown's quotation style.
     */
    fun pasteAsQuotation() {
        PasteAsConfirmationDialogFragment.show(binding.root.context)
    }

    /**
     * Go to top.
     */
    fun pageUp() {
        moveToIndex(0)
    }

    /**
     * Go to bottom.
     */
    fun pageDown() {
        moveToIndex(binding.editorInput.length())
    }

    private fun moveToIndex(index: Int) {
        requestFocusInputArea()
        binding.editorInput.setSelection(index)
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
    fun saveAs() {
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
    fun load() {
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

        if (TextUtils.equals(file.absolutePath, path)) {
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
        binding.lastSaved.setText(lastSavedTitle + dateFormatHolder.get().format(calendar.time))
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
     * Show menu's name.
     *
     * @param view [View] (TextView)
     */
    fun showName(view: View): Boolean {
        if (view is TextView) {
            Toaster.withAction(
                    binding.snackbarContainer,
                    view.text.toString(),
                    R.string.run,
                    View.OnClickListener { view.performClick() },
                    preferenceApplier.colorPair(),
                    Snackbar.LENGTH_LONG
            )
        }
        return true
    }

    /**
     * Show snackbar with specified id text.
     *
     * @param id
     */
    private inline fun snackText(@StringRes id: Int) {
        Toaster.snackShort(binding.snackbarContainer, id, preferenceApplier.colorPair())
    }

    /**
     * Show message by [android.support.design.widget.Snackbar].
     *
     * @param message
     */
    private fun snackText(message: String) {
        Toaster.snackShort(
                binding.snackbarContainer,
                message,
                preferenceApplier.colorPair()
        )
    }

    /**
     * Insert text to [EditText].
     * @param text insert text
     */
    fun insert(text: CharSequence?) {
        binding.editorInput.text.insert(binding.editorInput.selectionStart, text)
    }

    override fun hide() {
        super.hide()
        if (path.isNotEmpty()) {
            saveToFile(path)
        }
    }

    fun find(text: String) {
        binding.editorInput.text.indexOf(text, lastIndex)
    }

    fun findUp(text: String) {
        if (lastIndex >= 0) {
            selectTextByIndex(findBackwardIndex(text), text);
        }
        val nextBackwardIndex = findBackwardIndex(text)
        if (nextBackwardIndex == -1) {
            lastIndex = binding.editorInput.text.length
        }
    }

    private fun findBackwardIndex(text: String): Int {
        val index = lastIndex - text.length - 1
        if (index < 0) {
            return -1
        }
        val haystack = binding.editorInput.text.toString()
        return haystack.lastIndexOf(text, index)
    }

    fun findDown(text: String) {
        selectTextByIndex(findNextForwardIndex(text), text)
        val nextForwardIndex = findNextForwardIndex(text)
        if (nextForwardIndex == -1) {
            lastIndex = 0
        }
    }

    private fun selectTextByIndex(index: Int, text: String) {
        if (index < 0) {
            lastIndex = 0
            return
        }
        requestFocusInputArea()
        lastIndex = index + text.length
        binding.editorInput.setSelection(index, lastIndex)
    }

    private fun findNextForwardIndex(text: String) =
            binding.editorInput.text.indexOf(text, lastIndex)

    private fun requestFocusInputArea() {
        binding.editorInput.requestFocus()
    }

    companion object {

        /**
         * Request code of specifying file.
         */
        const val REQUEST_CODE_LOAD: Int = 10111

    }

}