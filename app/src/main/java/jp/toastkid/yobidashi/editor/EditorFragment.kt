/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.editor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserViewModel
import jp.toastkid.yobidashi.databinding.FragmentEditorBinding
import jp.toastkid.yobidashi.databinding.ModuleFragmentEditorMenuBinding
import jp.toastkid.yobidashi.libs.*
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.speech.SpeechMaker
import jp.toastkid.yobidashi.main.HeaderViewModel
import okio.Okio
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * saveTabCallback
 * { file ->
val currentTab = tabs.currentTab()
if (currentTab is EditorTab) {
currentTab.setFileInformation(file)
tabs.saveTabList()
}
}
 * @author toastkidjp
 */
class EditorFragment : Fragment() {

    private lateinit var binding: FragmentEditorBinding

    private lateinit var menuBinding: ModuleFragmentEditorMenuBinding

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    private val thumbnailGenerator = ThumbnailGenerator()

    /**
     * Default date format holder.
     */
    private val dateFormatHolder: ThreadLocal<DateFormat> by lazy {
        object: ThreadLocal<DateFormat>() {
            override fun initialValue(): DateFormat =
                    SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        }
    }

    private lateinit var speechMaker: SpeechMaker

    /**
     * Last saved text.
     */
    private lateinit var lastSavedTitle: String

    /**
     * File path.
     */
    private var path: String = ""

    /**
     * Text finder for [EditText].
     */
    private lateinit var finder: EditTextFinder

    private var headerViewModel: HeaderViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_editor, container, false)
        menuBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.module_fragment_editor_menu,
                container,
                false
        )
        menuBinding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return

        preferenceApplier = PreferenceApplier(context)
        finder = EditTextFinder(binding.editorInput)

        speechMaker = SpeechMaker(context)

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
                    val menuInflater = MenuInflater(context)
                    menuInflater.inflate(R.menu.context_editor, menu)
                    menuInflater.inflate(R.menu.context_speech, menu)
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
                            PasteAsConfirmationDialogFragment.show(context)
                            actionMode?.finish()
                            return true
                        }
                        R.id.context_edit_speech -> {
                            speechMaker.invoke(content())
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

        val browserViewModel = (context as? FragmentActivity)?.let { fragmentActivity ->
            ViewModelProviders.of(fragmentActivity)
                    .get(BrowserViewModel::class.java)
        }

        binding.editorInput.customSelectionActionModeCallback = object : ActionMode.Callback {

            override fun onCreateActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
                val text = extractSelectedText()
                if (Urls.isValidUrl(text)) {
                    MenuInflater(context).inflate(R.menu.context_editor_url, menu)
                }
                MenuInflater(context).inflate(R.menu.context_speech, menu)
                return true
            }

            override fun onActionItemClicked(actionMode: ActionMode?, menuItem: MenuItem?): Boolean {
                val text = extractSelectedText()
                when (menuItem?.itemId) {
                    R.id.context_edit_url_open_new -> {
                        browserViewModel?.open(text.toUri())
                        actionMode?.finish()
                        return true
                    }
                    R.id.context_edit_url_open_background -> {
                        browserViewModel?.openBackground(text.toUri())
                        actionMode?.finish()
                        return true
                    }
                    R.id.context_edit_url_preview -> {
                        browserViewModel?.preview(text.toUri())
                        Inputs.hideKeyboard(binding.root)
                        actionMode?.finish()
                        return true
                    }
                    R.id.context_edit_speech -> {
                        speechMaker.invoke(text)
                        actionMode?.finish()
                        return true
                    }
                    else -> Unit
                }
                actionMode?.finish()
                return false
            }

            private fun extractSelectedText(): String {
                return binding.editorInput.text
                        .subSequence(binding.editorInput.selectionStart, binding.editorInput.selectionEnd)
                        .toString()
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?) = true

            override fun onDestroyActionMode(p0: ActionMode?) = Unit

        }

        lastSavedTitle = context.getString(R.string.last_saved)

        (context as? FragmentActivity)?.let {
            headerViewModel = ViewModelProviders.of(it).get(HeaderViewModel::class.java)
        }
    }

    override fun onResume() {
        super.onResume()
        applySettings()
        val view = menuBinding.root
        headerViewModel?.replace(view)
    }

    override fun onDetach() {
        super.onDetach()
        if (path.isNotEmpty()) {
            saveToFile(path)
        }
    }

    /**
     * Apply color and font setting.
     */
    private fun applySettings() {
        val colorPair = preferenceApplier.colorPair()
        applyButtonColor(
                colorPair,
                menuBinding.save,
                menuBinding.saveAs,
                menuBinding.load,
                menuBinding.loadAs,
                menuBinding.lastSaved,
                menuBinding.counter,
                menuBinding.backup,
                menuBinding.clear
        )

        menuBinding.editorMenu.setBackgroundColor(colorPair.bgColor())

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
        menuBinding.counter?.text =
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
        startActivityForResult(IntentFactory.makeGetContent("text/plain"), REQUEST_CODE_LOAD_AS)
    }

    /**
     * Share current content.
     */
    fun share() {
        val title =
                if (path.contains("/")) path.substring(path.lastIndexOf("/") + 1)
                else path
        startActivity(IntentFactory.makeShare(content(), title))
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
        val context = context ?: return
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
        startActivityForResult(IntentFactory.makeGetContent("text/plain"), REQUEST_CODE_LOAD)
    }

    /**
     * Read content from file [Uri].
     *
     * @param data [Uri]
     */
    fun readFromFileUri(data: Uri) {
        val context = context ?: return

        FileExtractorFromUri(context, data)?.let {
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
        //TODO saveTabCallback(file)

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
        menuBinding.lastSaved?.setText(lastSavedTitle + dateFormatHolder.get()?.format(calendar.time))
    }

    /**
     * Clear current file path and reset edit-text.
     */
    fun clearPath() {
        path = ""
        clearInput()
        menuBinding.lastSaved?.setText("")
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
        context?.let { setContentTextLengthCount(it) }
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
        val context = context ?: return
        var newFile = assignFile(context, fileName)
        while (newFile.exists()) {
            newFile = assignFile(
                    context,
                    "${removeExtension(newFile.name)}_.txt"
            )
        }
        path = newFile.absolutePath
        //TODO saveTabCallback(newFile)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (resultCode != Activity.RESULT_OK || intent == null) {
            return
        }

        when (resultCode) {
            EditorModule.REQUEST_CODE_LOAD -> {
                intent.data?.let { readFromFileUri(it) }
            }
            EditorModule.REQUEST_CODE_LOAD_AS -> {
                intent.data?.let {
                    readFromFileUri(it)
                    saveAs()
                }
            }
        }
    }

    companion object {

        /**
         * Request code of specifying file.
         */
        const val REQUEST_CODE_LOAD: Int = 10111

        /**
         * Request code for 'Load as'.
         */
        const val REQUEST_CODE_LOAD_AS: Int = 10112

    }
}