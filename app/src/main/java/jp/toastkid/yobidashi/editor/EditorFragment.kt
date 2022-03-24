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
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import jp.toastkid.article_viewer.article.data.ArticleInsertion
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.FileExtractorFromUri
import jp.toastkid.lib.TabListViewModel
import jp.toastkid.lib.dialog.ConfirmDialogFragment
import jp.toastkid.lib.fragment.CommonFragmentAction
import jp.toastkid.lib.intent.GetContentIntentFactory
import jp.toastkid.lib.intent.ShareIntentFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.storage.ExternalFileAssignment
import jp.toastkid.lib.tab.TabUiFragment
import jp.toastkid.lib.viewmodel.PageSearcherViewModel
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.editor.load.LoadFromStorageDialogUi
import jp.toastkid.yobidashi.editor.load.StorageFilesFinder
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.speech.SpeechMaker
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.util.Calendar

/**
 * @author toastkidjp
 */
class EditorFragment :
    Fragment(),
    TabUiFragment,
    CommonFragmentAction,
    ContentScrollable
{

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    private val externalFileAssignment = ExternalFileAssignment()

    private var speechMaker: SpeechMaker? = null

    /**
     * Last saved text.
     */
    private lateinit var lastSavedTitle: String

    /**
     * File path.
     */
    private var path: String = ""

    private val contentHolderService = ContentHolderService()

    /**
     * Text finder for [EditText].
     */
    private lateinit var finder: EditTextFinder

    private var appBarViewModel: AppBarViewModel? = null

    private var tabListViewModel: TabListViewModel? = null

    private var contentViewModel: ContentViewModel? = null

    private var loadAs: ActivityResultLauncher<Intent>? =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }

            it.data?.data?.let { uri ->
                readFromFileUri(uri)
                saveAs()
            }
        }

    private var loadResultLauncher: ActivityResultLauncher<Intent>? =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }

            it.data?.data?.let { uri -> readFromFileUri(uri) }
        }

    private var editorInput: MutableState<String>? = null

    private var openLoadFromStorageDialog: MutableState<Boolean>? = null

    private var openInputFileNameDialog: MutableState<Boolean>? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val context = activity ?: return super.onCreateView(inflater, container, savedInstanceState)
        val composeView = ComposeView(context)
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        /*
            android:inputType="textMultiLine"
            android:minLines="10"
         */
        composeView.setContent {
            val editorInput = remember { mutableStateOf("") }
            this.editorInput = editorInput

            val openLoadFromStorageDialog = remember { mutableStateOf(false) }
            this.openLoadFromStorageDialog = openLoadFromStorageDialog

            val openInputFileNameDialog = remember { mutableStateOf(false) }
            this.openInputFileNameDialog = openInputFileNameDialog

            TextField(
                value = editorInput.value,
                onValueChange = { newInput ->
                    editorInput.value = newInput
                    setContentTextLengthCount(context)
                },
                label = { stringResource(id = R.string.hint_editor_input) },
                textStyle = TextStyle(
                    color = Color(preferenceApplier.editorFontColor()),
                    fontSize = preferenceApplier.editorFontSize().toFloat().sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(8f, 8f),
                        blurRadius = 4f
                    ),
                    textGeometricTransform = TextGeometricTransform(
                        scaleX = 2.5f,
                        skewX = 1f
                    )
                ),
                modifier = Modifier
                    .background(Color(preferenceApplier.editorBackgroundColor()))
                    .padding(start = 4.dp, end = 4.dp)
            )

            if (openLoadFromStorageDialog.value) {
                LoadFromStorageDialogUi(
                    openDialog = openLoadFromStorageDialog,
                    files = StorageFilesFinder().invoke(context),
                    onSelect = { readFromFileUri(Uri.fromFile(it)) }
                )
            }

            if (openInputFileNameDialog.value) {
                val currentName = File(path).nameWithoutExtension ?: ""
                InputFileNameDialogUi(
                    openDialog = openInputFileNameDialog,
                    defaultInput = currentName,
                    onCommit = {
                        if (it.isBlank()) {
                            return@InputFileNameDialogUi
                        }
                        assignNewFile(it)
                    }
                )
            }
        }
/*
        CursorColorSetter().invoke(binding.editorInput, preferenceApplier.editorCursorColor(ContextCompat.getColor(binding.root.context, R.color.editor_cursor)))
        binding.editorInput.highlightColor = preferenceApplier.editorHighlightColor(ContextCompat.getColor(binding.root.context, R.color.light_blue_200_dd))
 */
        ViewModelProvider(context).get(AppBarViewModel::class.java)
            .replace(context) {
                Row(
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth()
                        .horizontalScroll(
                            rememberScrollState()
                        )
                ) {
                    EditorMenuItem(R.string.load, R.drawable.ic_load) { load() }

                    EditorMenuItem(R.string.save, R.drawable.ic_save) { save() }

                    EditorMenuItem(R.string.save_backup, R.drawable.ic_backup) { backup() }

                    EditorMenuItem(R.string.save_as, R.drawable.ic_save_as) { saveAs() }

                    Box(modifier = Modifier
                        .width(60.dp)
                        .fillMaxHeight()
                        .clickable {
                            tabList()
                        }) {
                        Image(
                            painterResource(R.drawable.ic_tab),
                            contentDescription = stringResource(id = R.string.tab_list),
                            colorFilter = ColorFilter.tint(Color(preferenceApplier.fontColor), BlendMode.SrcIn),
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .padding(12.dp)
                        )
                        Text(text = "",
                            color = Color(preferenceApplier.fontColor),
                            fontSize = 12.sp)
                    }

                    EditorMenuItem(R.string.restore, R.drawable.ic_restore) { restore() }

                    EditorMenuItem(R.string.load_as, R.drawable.ic_load_as) { loadAs() }

                    EditorMenuItem(R.string.load_from_storage, R.drawable.ic_load) { loadFromStorage() }

                    EditorMenuItem(R.string.export_to_article_viewer, R.drawable.ic_article) { exportToArticleViewer() }

                    Text(
                        text = "",
                        color = Color(preferenceApplier.fontColor),
                        fontSize = 14.sp,
                        maxLines = 2,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(start = 4.dp, end = 4.dp)
                    )

                    Text(
                        text = "",
                        color = Color(preferenceApplier.fontColor),
                        fontSize = 14.sp,
                        maxLines = 2,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(start = 4.dp, end = 4.dp)
                    )

                    EditorMenuItem(R.string.clear_all, R.drawable.ic_clear_form) { clear() }
                }
            }

        return composeView
    }

    @Composable
    private fun EditorMenuItem(
        labelId: Int,
        iconId: Int,
        onClick: () -> Unit
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(60.dp)
                .background(Color(preferenceApplier.color))
                .fillMaxHeight()
                .clickable {
                    onClick()
                }
        ) {
            Image(
                painter = painterResource(id = iconId),
                contentDescription = stringResource(id = labelId),
                colorFilter = ColorFilter.tint(Color(preferenceApplier.fontColor), BlendMode.SrcIn)
            )
            Text(
                text = stringResource(id = labelId),
                color = Color(preferenceApplier.fontColor),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return

        preferenceApplier = PreferenceApplier(context)
        //TODO finder = EditTextFinder(binding.editorInput)

        speechMaker = SpeechMaker(context)

        lastSavedTitle = context.getString(R.string.last_saved)

        activity?.let { activity ->
            val viewModelProvider = ViewModelProvider(activity)
            appBarViewModel = viewModelProvider.get(AppBarViewModel::class.java)
            tabListViewModel = viewModelProvider.get(TabListViewModel::class.java)

            //TODO EditorContextMenuInitializer().invoke(binding.editorInput, speechMaker, viewModelProvider)

            tabListViewModel
                    ?.tabCount
                    //TODO ?.observe(activity, { menuBinding.tabCount.text = it.toString() })

            (viewModelProvider.get(PageSearcherViewModel::class.java)).let { viewModel ->
                var currentWord = ""
                viewModel.find.observe(viewLifecycleOwner, {
                    if (currentWord != it) {
                        currentWord = it
                    }
                    finder.findDown(currentWord)
                })

                viewModel.upward.observe(viewLifecycleOwner, Observer {
                    if (currentWord != it) {
                        currentWord = it
                    }
                    finder.findUp(currentWord)
                })

                viewModel.downward.observe(viewLifecycleOwner, Observer {
                    if (currentWord != it) {
                        currentWord = it
                    }
                    finder.findDown(currentWord)
                })
            }

            contentViewModel = viewModelProvider.get(ContentViewModel::class.java)
        }

        parentFragmentManager.setFragmentResultListener(
            "clear_input",
            viewLifecycleOwner,
            { _, _ -> clearInput() }
        )

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

    override fun onPause() {
        super.onPause()
        saveIfNeed()
    }

    override fun onDetach() {
        if (path.isNotEmpty()) {
            saveToFile(path)
        }
        speechMaker?.dispose()

        loadAs?.unregister()
        loadResultLauncher?.unregister()

        parentFragmentManager.clearFragmentResultListener("clear_input")

        super.onDetach()
    }

    /**
     * Set content text length count to binding.counter.
     *
     * @param context Context
     */
    private fun setContentTextLengthCount(context: Context) {
        //TODO menuBinding.counter.text =
                context.getString(R.string.message_character_count, content().length)
    }

    fun tabList() {
        contentViewModel?.switchTabList()
    }

    fun openNewTab(): Boolean {
        tabListViewModel?.openNewTab()
        return true
    }

    /**
     * Backup current file.
     */
    fun backup() {
        if (path.isEmpty()) {
            save()
            return
        }
        val fileName = File(path).nameWithoutExtension + "_backup.txt"
        val context = context ?: return
        saveToFile(externalFileAssignment(context, fileName).absolutePath)
    }

    fun clear() {
        ConfirmDialogFragment.show(
            parentFragmentManager,
            getString(R.string.title_clear_text),
            Html.fromHtml(
                getString(R.string.confirm_clear_all_settings),
                Html.FROM_HTML_MODE_COMPACT
            ),
            "clear_input"
        )
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
        moveToIndex(editorInput?.value?.length ?: 0)
    }

    /**
     * Move cursor to specified index.
     *
     * @param index index of editor.
     */
    private fun moveToIndex(index: Int) {
        //binding.editorInput.requestFocus()
        //TODO binding.editorInput.setSelection(index)
    }

    /**
     * Save current content to file.
     */
    fun save() {
        if (path.isNotEmpty()) {
            saveToFile(path)
            return
        }

        openInputFileNameDialog?.value = true
    }

    /**
     * Save current text as other file.
     */
    fun saveAs() {
        openInputFileNameDialog?.value = true
    }

    /**
     * Load text as other file.
     */
    fun loadAs() {
        loadAs?.launch(GetContentIntentFactory()("text/plain"))
    }

    fun loadFromStorage() {
        openLoadFromStorageDialog?.value = true
    }

    fun exportToArticleViewer() {
        val context = context ?: return
        ArticleInsertion(context).invoke(
                if (path.isEmpty()) Calendar.getInstance().time.toString() else path.split("/").last(),
                content()
        )
        contentViewModel?.snackShort(R.string.done_save)
    }

    fun restore() {
        /*TODO RestoreContentUseCase(
            contentHolderService,
            contentViewModel,
            binding.editorInput,
            ::setContentText
        ).invoke()*/
    }

    /**
     * Share current content.
     */
    override fun share() {
        val title =
                if (path.contains("/")) path.substring(path.lastIndexOf("/") + 1)
                else path
        startActivity(ShareIntentFactory()(content(), title))
    }

    /**
     * Call from fragment's onPause().
     */
    private fun saveIfNeed() {
        if (path.isNotEmpty()) {
            saveToFile(path)
        }
    }

    private fun saveToFile(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            file.createNewFile()
        }

        val content = content()
        contentHolderService.setContent(content)
        file.sink().use { sink ->
            sink.buffer().use { bufferedSink ->
                bufferedSink.writeUtf8(content)
            }
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
        loadResultLauncher?.launch(GetContentIntentFactory()("text/plain"))
    }

    /**
     * Read content from file [Uri].
     *
     * @param data [Uri]
     */
    private fun readFromFileUri(data: Uri) {
        val context = context ?: return

        FileExtractorFromUri(context, data)?.let {
            if (it == path) {
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

        val text = file.source().use { source ->
            source.buffer().use { bufferedSource ->
                bufferedSource.readUtf8()
            }
        }
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
        //TODO menuBinding.lastSaved.text = DateFormat.format("$lastSavedTitle HH:mm:ss", ms)
    }

    /**
     * Clear current file path and reset edit-text.
     */
    private fun clearPath() {
        path = ""
        clearInput()
        //TODO menuBinding.lastSaved.text = ""
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
        editorInput?.value = contentStr
        context?.let { setContentTextLengthCount(it) }
        contentHolderService.setContent(contentStr)
    }

    /**
     * Return current content.
     *
     * @return content [String]
     */
    private fun content(): String = editorInput?.value ?: ""

    /**
     * Assign new file object.
     *
     * @param fileName
     */
    fun assignNewFile(fileName: String) {
        val context = context ?: return
        var newFile = externalFileAssignment(context, fileName)
        while (newFile.exists()) {
            newFile = externalFileAssignment(
                    context,
                    "${newFile.nameWithoutExtension}_.txt"
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
                { view.performClick() },
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
        //TODO editorInput.insert(binding.editorInput.selectionStart, text)
    }

}