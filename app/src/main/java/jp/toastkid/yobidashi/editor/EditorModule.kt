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
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.view.animation.Animation
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.Dimension
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModuleEditorBinding
import jp.toastkid.yobidashi.databinding.ModuleEditorMenuBinding
import jp.toastkid.yobidashi.libs.FileExtractorFromUri
import jp.toastkid.yobidashi.libs.ThumbnailGenerator
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.main.HeaderViewModel
import okio.Okio
import timber.log.Timber
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
) {

    /**
     * Preferences wrapper.
     */
    private val preferenceApplier: PreferenceApplier = PreferenceApplier(binding.root.context)

    private val thumbnailGenerator = ThumbnailGenerator()

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
     * Text finder for [EditText].
     */
    private var finder: EditTextFinder

    private var menuBinding: ModuleEditorMenuBinding? = null

    private var headerViewModel: HeaderViewModel? = null

    init {
        val context = binding.root.context

        binding.editorModule = this

        finder = EditTextFinder(binding.editorInput)

        binding.editorInput.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(contentEditable: Editable?) {
                setContentTextLengthCount(context)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.editorInput.customInsertionActionModeCallback = object : ActionMode.Callback {

                override fun onCreateActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
                    MenuInflater(context).inflate(R.menu.context_editor, menu)
                    return true
                }

                override fun onActionItemClicked(actionMode: ActionMode?, menu: MenuItem?): Boolean {
                    when (menu?.itemId) {
                        R.id.context_edit_insert_as_plain -> {
                            insertAsPlain()
                            actionMode?.finish()
                            return true
                        }
                        R.id.context_edit_paste_as_quotation -> {
                            pasteAsQuotation()
                            actionMode?.finish()
                            return true
                        }
                        else -> Unit
                    }
                    actionMode?.finish()
                    return false
                }

                override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?) = true

                override fun onDestroyActionMode(p0: ActionMode?) = Unit

            }
        }

        lastSavedTitle = context.getString(R.string.last_saved)

        menuBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.module_editor_menu,
                null,
                false
        )
        menuBinding?.editorModule = this

        (context as? FragmentActivity)?.let {
            headerViewModel = ViewModelProviders.of(it).get(HeaderViewModel::class.java)
        }
    }

    /**
     * Apply color and font setting.
     */
    fun applySettings() {
        val mb = menuBinding ?: return
        val colorPair = preferenceApplier.colorPair()
        applyButtonColor(
                colorPair,
                mb.save,
                mb.saveAs,
                mb.load,
                mb.loadAs,
                mb.share,
                mb.lastSaved,
                mb.counter,
                mb.backup,
                mb.clear
                )

        mb.editorMenu.setBackgroundColor(colorPair.bgColor())

        binding.editorScroll.setBackgroundColor(preferenceApplier.editorBackgroundColor())
        binding.editorInput.setTextColor(preferenceApplier.editorFontColor())
        binding.editorInput.setTextSize(Dimension.SP, preferenceApplier.editorFontSize().toFloat())

        CursorColorSetter().invoke(binding.editorInput, preferenceApplier.editorCursorColor())
        binding.editorInput.highlightColor = preferenceApplier.editorHighlightColor()
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
            textView.compoundDrawables.forEach {
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
        menuBinding?.counter?.text =
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

    fun insertAsPlain() {
        val primary = Clipboard.getPrimary(binding.root.context)
        if (TextUtils.isEmpty(primary)) {
            return
        }
        insert(primary)
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

    /**
     * Find text in bound to upward.
     *
     * @param text Finding text
     */
    fun findUp(text: String) = finder.findUp(text)

    /**
     * Find text in bound to downward.
     *
     * @param text Finding text
     */
    fun findDown(text: String) = finder.findDown(text)

    /**
     * Move cursor to specified index.
     *
     * @param index index of editor.
     */
    private fun moveToIndex(index: Int) {
        binding.editorInput.requestFocus()
        binding.editorInput.setSelection(index)
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

        InputNameDialogFragment.show(binding.root.context)
    }

    /**
     * Save current text as other file.
     */
    fun saveAs() {
        InputNameDialogFragment.show(binding.root.context)
    }

    /**
     * Load text as other file.
     */
    fun loadAs() {
        intentLauncher(IntentFactory.makeGetContent("text/plain"), REQUEST_CODE_LOAD_AS)
    }

    /**
     * Share current content.
     */
    fun share() {
        val title =
                if (path.contains("/")) path.substring(path.lastIndexOf("/") + 1)
                else path
        intentLauncher(IntentFactory.makeShare(content(), title), REQUEST_CODE_SHARE)
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
    private fun removeExtension(fileName: String): String {
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
    private fun assignFile(context: Context, fileName: String): File {
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
                null
        ) { _, _ ->  }
        snackText("${context.getString(R.string.done_save)}: $filePath")
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
        menuBinding?.lastSaved?.setText(lastSavedTitle + dateFormatHolder.get()?.format(calendar.time))
    }

    /**
     * Clear current file path and reset edit-text.
     */
    fun clearPath() {
        path = ""
        clearInput()
        menuBinding?.lastSaved?.setText("")
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
    private fun content(): String = binding.editorInput.text.toString()

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
    fun makeThumbnail(): Bitmap? = thumbnailGenerator(binding.root)

    /**
     * Assign new file object.
     *
     * @param fileName
     */
    fun assignNewFile(fileName: String) {
        val context = binding.root.context
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
                    binding.editorInput,
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
    private fun snackText(@StringRes id: Int) {
        Toaster.snackShort(binding.editorInput, id, preferenceApplier.colorPair())
    }

    /**
     * Show message by [com.google.android.material.snackbar.Snackbar].
     *
     * @param message
     */
    private fun snackText(message: String) {
        Toaster.snackShort(
                binding.editorInput,
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

    fun show() {
        if (binding.root.visibility == View.GONE) {
            Completable.fromAction {
                binding.root.visibility = View.VISIBLE
            }.subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe({}, Timber::e)
        }
        val view = menuBinding?.root ?: return
        headerViewModel?.replace(view)
    }

    fun hide() {
        if (binding.root.visibility == View.VISIBLE) {
            Completable.fromAction {
                binding.root.visibility = View.GONE
            }.subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe({}, Timber::e)
        }
        if (path.isNotEmpty()) {
            saveToFile(path)
        }
    }

    fun isVisible() = binding.root.isVisible

    companion object {

        /**
         * Request code of specifying file.
         */
        const val REQUEST_CODE_LOAD: Int = 10111

        /**
         * Request code for 'Load as'.
         */
        const val REQUEST_CODE_LOAD_AS: Int = 10112

        /**
         * Request code for 'Share'.
         */
        const val REQUEST_CODE_SHARE: Int = 10113

    }

}