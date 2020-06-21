/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.editor

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
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
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.Dimension
import androidx.annotation.StringRes
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserViewModel
import jp.toastkid.yobidashi.browser.page_search.PageSearcherViewModel
import jp.toastkid.yobidashi.databinding.FragmentEditorBinding
import jp.toastkid.yobidashi.databinding.ModuleFragmentEditorMenuBinding
import jp.toastkid.yobidashi.libs.FileExtractorFromUri
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.permission.RuntimePermissions
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.speech.SpeechMaker
import jp.toastkid.yobidashi.main.ContentScrollable
import jp.toastkid.yobidashi.main.HeaderViewModel
import jp.toastkid.yobidashi.main.MainActivity
import jp.toastkid.yobidashi.main.TabUiFragment
import jp.toastkid.yobidashi.main.content.ContentViewModel
import jp.toastkid.yobidashi.tab.tab_list.TabListViewModel
import okio.Okio
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * TODO Modify behavior on switched new tab.
 * @author toastkidjp
 */
class EditorFragment :
        Fragment(),
        TabUiFragment,
        PasteAsConfirmationDialogFragment.Callback,
        ClearTextDialogFragment.Callback,
        InputNameDialogFragment.Callback,
        CommonFragmentAction,
        ContentScrollable
{

    private lateinit var binding: FragmentEditorBinding

    private lateinit var menuBinding: ModuleFragmentEditorMenuBinding

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    /**
     * Default date format holder.
     */
    private val dateFormatHolder: ThreadLocal<SimpleDateFormat> by lazy {
        object: ThreadLocal<SimpleDateFormat>() {
            override fun initialValue() =
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

    private lateinit var runtimePermissions: RuntimePermissions

    private var headerViewModel: HeaderViewModel? = null

    private var tabListViewModel: TabListViewModel? = null

    private var contentViewModel: ContentViewModel? = null

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

        runtimePermissions = RuntimePermissions(requireActivity())

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
                    return false
                }

                override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?) = true

                override fun onDestroyActionMode(p0: ActionMode?) = Unit

            }
        }

        val browserViewModel = activity?.let { fragmentActivity ->
            ViewModelProvider(fragmentActivity)
                    .get(BrowserViewModel::class.java)
        }

        binding.editorInput.customSelectionActionModeCallback = object : ActionMode.Callback {

            private val listHeadAdder = ListHeadAdder()

            override fun onCreateActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
                val text = extractSelectedText()
                if (Urls.isValidUrl(text)) {
                    MenuInflater(context).inflate(R.menu.context_editor_url, menu)
                }
                MenuInflater(context).inflate(R.menu.context_editor_selected, menu)
                MenuInflater(context).inflate(R.menu.context_speech, menu)
                return true
            }

            override fun onActionItemClicked(actionMode: ActionMode?, menuItem: MenuItem?): Boolean {
                val text = extractSelectedText()
                when (menuItem?.itemId) {
                    R.id.context_edit_add_order -> {
                        listHeadAdder(binding.editorInput, "1.")
                        return true
                    }
                    R.id.context_edit_add_minus -> {
                        listHeadAdder(binding.editorInput, "-")
                        return true
                    }
                    R.id.context_edit_add_quote -> {
                        listHeadAdder(binding.editorInput, ">")
                        return true
                    }
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

        activity?.let { activity ->
            val viewModelProvider = ViewModelProvider(activity)
            headerViewModel = viewModelProvider.get(HeaderViewModel::class.java)
            tabListViewModel = viewModelProvider.get(TabListViewModel::class.java)

            tabListViewModel
                    ?.tabCount
                    ?.observe(activity, Observer { menuBinding.tabCount.setText(it.toString()) })

            (viewModelProvider.get(PageSearcherViewModel::class.java)).let { viewModel ->
                var currentWord = ""
                viewModel.find.observe(activity, Observer {
                    currentWord = it ?: ""
                    finder.findDown(currentWord)
                })

                viewModel.upward.observe(activity, Observer {
                    finder.findUp(currentWord)
                })

                viewModel.downward.observe(activity, Observer {
                    finder.findDown(currentWord)
                })
            }

            contentViewModel = viewModelProvider.get(ContentViewModel::class.java)
        }

        reload()
    }

    fun reload() {
        if (arguments?.containsKey("path") == true) {
            path = arguments?.getString("path") ?: ""
        }

        if (path.isEmpty()) {
            clearInput()
        } else {
            readFromFile(File(path))
        }
    }

    override fun onResume() {
        super.onResume()
        applySettings()
        val view = menuBinding.root
        headerViewModel?.replace(view)
    }

    override fun onPause() {
        super.onPause()
        saveIfNeed()
    }

    override fun onDetach() {
        if (path.isNotEmpty()) {
            saveToFile(path)
        }
        speechMaker.dispose()
        super.onDetach()
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

        menuBinding.tabIcon.setColorFilter(colorPair.fontColor())
        menuBinding.tabCount.setTextColor(colorPair.fontColor())

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

    fun tabList() {
        contentViewModel?.switchTabList()
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
        ClearTextDialogFragment.show(this)
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
    override fun toTop() {
        moveToIndex(0)
    }

    /**
     * Go to bottom.
     */
    override fun toBottom() {
        moveToIndex(binding.editorInput.length())
    }

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
     * Save current content to file.
     */
    fun save() {
        if (path.isNotEmpty()) {
            saveToFile(path)
            return
        }

        InputNameDialogFragment.show(this)
    }

    /**
     * Save current text as other file.
     */
    fun saveAs() {
        InputNameDialogFragment.show(this)
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
    override fun share() {
        val title =
                if (path.contains("/")) path.substring(path.lastIndexOf("/") + 1)
                else path
        startActivity(IntentFactory.makeShare(content(), title))
    }

    /**
     * Call from fragment's onPause().
     */
    private fun saveIfNeed() {
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

        if (externalFilesDir?.exists() == false) {
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

        if (runtimePermissions.isRevoked(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            snackText(R.string.message_requires_permission_storage)
            return
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

        if (isVisible) {
            snackText("${context.getString(R.string.done_save)}: $filePath")
        }

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
    private fun readFromFileUri(data: Uri) {
        val context = context ?: return

        FileExtractorFromUri(context, data)?.let {
            if (TextUtils.equals(it, path)) {
                return
            }

            readFromFile(File(it))
        }
    }

    /**
     * Read content from [File].
     *
     * @param file [File]
     */
    private fun readFromFile(file: File) {
        if (!file.exists() || !file.canRead()) {
            snackText(R.string.message_cannot_read_file)
            clearPath()
            return
        }

        val text = Okio.buffer(Okio.source(file)).use { it.readUtf8() }
        setContentText(text)
        snackText(R.string.done_load)
        path = file.absolutePath
        tabListViewModel?.saveEditorTab(file)

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
        menuBinding.lastSaved.text = lastSavedTitle + dateFormatHolder.get()?.format(calendar.time)
    }

    /**
     * Clear current file path and reset edit-text.
     */
    private fun clearPath() {
        path = ""
        clearInput()
        menuBinding.lastSaved.text = ""
    }

    /**
     * Clear input text.
     */
    private fun clearInput() {
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
        binding.root.startAnimation(animation)
    }

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
        tabListViewModel?.saveEditorTab(newFile)
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
                    view,
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
        contentViewModel?.snackShort(id)
    }

    /**
     * Show message by [com.google.android.material.snackbar.Snackbar].
     *
     * @param message
     */
    private fun snackText(message: String) {
        contentViewModel?.snackShort(message)
    }

    /**
     * Insert text to [EditText].
     * @param text insert text
     */
    fun insert(text: CharSequence?) {
        binding.editorInput.text.insert(binding.editorInput.selectionStart, text)
    }

    override fun onClickPasteAs() {
        val activityContext = context ?: return
        val primary = Clipboard.getPrimary(activityContext)
        if (TextUtils.isEmpty(primary)) {
            return
        }
        insert(Quotation()(primary))
    }

    override fun onClickClearInput() {
        clearInput()
    }

    override fun onClickInputName(fileName: String) {
        assignNewFile(fileName)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (resultCode != Activity.RESULT_OK || intent == null) {
            return
        }

        when (requestCode) {
            REQUEST_CODE_LOAD -> {
                intent.data?.let { readFromFileUri(it) }
            }
            REQUEST_CODE_LOAD_AS -> {
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