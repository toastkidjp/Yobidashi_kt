/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.editor.view

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.text.format.DateFormat
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Dimension
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.TabListViewModel
import jp.toastkid.lib.intent.GetContentIntentFactory
import jp.toastkid.lib.intent.ShareIntentFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.viewmodel.PageSearcherViewModel
import jp.toastkid.ui.dialog.ConfirmDialog
import jp.toastkid.ui.dialog.DestructiveChangeConfirmDialog
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.editor.CursorColorSetter
import jp.toastkid.yobidashi.editor.EditTextFinder
import jp.toastkid.yobidashi.editor.EditorContextMenuInitializer
import jp.toastkid.yobidashi.editor.InputFileNameDialogUi
import jp.toastkid.yobidashi.editor.load.LoadFromStorageDialogUi
import jp.toastkid.yobidashi.editor.load.StorageFilesFinder
import jp.toastkid.yobidashi.editor.usecase.FileActionUseCase
import jp.toastkid.yobidashi.libs.speech.SpeechMaker

@Composable
fun EditorTabUi(path: String?) {
    val context = LocalContext.current as? ComponentActivity ?: return
    val preferenceApplier = PreferenceApplier(context)
    val viewModelProvider = ViewModelProvider(context)

    val editText = EditText(context)

    val finder = EditTextFinder(editText)

    val contentViewModel = viewModelProvider.get(ContentViewModel::class.java)
    val tabListViewModel = ViewModelProvider(context).get(TabListViewModel::class.java)

    val fileActionUseCase = FileActionUseCase(
        context,
        contentViewModel,
        remember { mutableStateOf(path ?: "") },
        { editText.text.toString() },
        { editText.setText(it) },
        { tabListViewModel.saveEditorTab(it) }
    )

    viewModelProvider.get(AppBarViewModel::class.java)
        .replace {
            AppBarContent(
                viewModelProvider.get(ContentViewModel::class.java),
                fileActionUseCase
            )
        }

    val localLifecycleOwner = LocalLifecycleOwner.current
    contentViewModel.toTop.observe(localLifecycleOwner, {
        it.getContentIfNotHandled() ?: return@observe
        editText.setSelection(0)
    })
    contentViewModel.toBottom.observe(localLifecycleOwner, {
        it.getContentIfNotHandled() ?: return@observe
        editText.setSelection(editText.text.length)
    })
    contentViewModel.share.observe(localLifecycleOwner, {
        it.getContentIfNotHandled() ?: return@observe
        val title =
            if (path?.contains("/") == true) path.substring(path.lastIndexOf("/") + 1)
            else path
        val content = editText.text.toString()
        if (content.isEmpty()) {
            contentViewModel.snackShort(R.string.error_content_is_empty)
            return@observe
        }
        context.startActivity(ShareIntentFactory().invoke(content, title))
    })

    AndroidView(
        factory = {
            EditorContextMenuInitializer().invoke(editText, SpeechMaker(it), viewModelProvider)
            editText.setBackgroundColor(Color.Transparent.toArgb())
            editText.setTextColor(preferenceApplier.editorFontColor())
            editText.setTextSize(Dimension.SP, preferenceApplier.editorFontSize().toFloat())
            editText.typeface = Typeface.MONOSPACE

            CursorColorSetter().invoke(
                editText,
                preferenceApplier.editorCursorColor(ContextCompat.getColor(context, R.color.editor_cursor))
            )
            editText.highlightColor = preferenceApplier.editorHighlightColor(
                ContextCompat.getColor(context, R.color.light_blue_200_dd)
            )
            editText
        },
        update = {
            fileActionUseCase.readCurrentFile()
        },
        modifier = Modifier
            .fillMaxSize()
            .background(Color(preferenceApplier.editorBackgroundColor()))
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    )

    val pageSearcherViewModel =
        viewModelProvider.get(PageSearcherViewModel::class.java)
    pageSearcherViewModel.upward.observe(context, {
        finder.findUp(it)
    })
    pageSearcherViewModel.downward.observe(context, {
        finder.findDown(it)
    })
    pageSearcherViewModel.find.observe(context, {
        finder.findDown(it)
    })

    val dialogState = remember { mutableStateOf(false) }
    BackHandler {
        dialogState.value = true
    }

    ConfirmDialog(
        dialogState,
        context.getString(R.string.confirmation),
        context.getString(R.string.message_confirm_exit)
    ) {
        context.finish()
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun AppBarContent(
    contentViewModel: ContentViewModel,
    fileActionUseCase: FileActionUseCase
) {
    val context = LocalContext.current as? ComponentActivity ?: return
    val preferenceApplier = PreferenceApplier(context)

    val openLoadFromStorageDialog = remember { mutableStateOf(false) }
    val openInputFileNameDialog = remember { mutableStateOf(false) }

    val tabListViewModel = ViewModelProvider(context).get(TabListViewModel::class.java)

    val loadAs: ActivityResultLauncher<Intent> =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) {
                return@rememberLauncherForActivityResult
            }

            it.data?.data?.let { uri ->
                fileActionUseCase.readFromFileUri(uri)
                openInputFileNameDialog?.value = true
            }
        }

    val openConfirmDialog = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
            .horizontalScroll(
                rememberScrollState()
            )
    ) {
        EditorMenuItem(R.string.load, R.drawable.ic_load) {
            loadAs.launch(GetContentIntentFactory()("text/plain"))
        }

        EditorMenuItem(R.string.save, R.drawable.ic_save) { fileActionUseCase.save(openInputFileNameDialog) }

        EditorMenuItem(R.string.save_as, R.drawable.ic_save_as) { openInputFileNameDialog?.value = true }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight()
                .combinedClickable(
                    true,
                    onClick = { contentViewModel.switchTabList() },
                    onLongClick = { tabListViewModel?.openNewTab() }
                )
        ) {
            Image(
                painterResource(R.drawable.ic_tab),
                contentDescription = stringResource(id = R.string.tab_list),
                colorFilter = ColorFilter.tint(Color(preferenceApplier.fontColor), BlendMode.SrcIn),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(12.dp)
            )
            Text(
                text = tabListViewModel.tabCount.observeAsState().value.toString(),
                color = Color(preferenceApplier.fontColor),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
            )
        }

        //EditorMenuItem(R.string.restore, R.drawable.ic_restore) { restore() }

        EditorMenuItem(R.string.load_from_storage, R.drawable.ic_load) {
            openLoadFromStorageDialog?.value = true
        }

        EditorMenuItem(R.string.export_to_article_viewer, R.drawable.ic_article) {
            fileActionUseCase.exportToArticleViewer()
            contentViewModel.snackShort(R.string.done_save)
        }

        Text(
            text = context.getString(R.string.last_saved) + DateFormat.format(" HH:mm:ss", fileActionUseCase.lastSaved.value),
            color = Color(preferenceApplier.fontColor),
            fontSize = 14.sp,
            maxLines = 2,
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 4.dp, end = 4.dp)
        )

        Text(
            text = context.getString(R.string.message_character_count, fileActionUseCase.getText().length),
            color = Color(preferenceApplier.fontColor),
            fontSize = 14.sp,
            maxLines = 2,
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 4.dp, end = 4.dp)
        )

        EditorMenuItem(R.string.clear_all, R.drawable.ic_clear_form) {
            openConfirmDialog.value = true
        }
    }

    if (openLoadFromStorageDialog.value) {
        LoadFromStorageDialogUi(
            openDialog = openLoadFromStorageDialog,
            files = StorageFilesFinder().invoke(context),
            onSelect = { fileActionUseCase.readFromFileUri(Uri.fromFile(it)) }
        )
    }

    InputFileNameDialogUi(
        openInputFileNameDialog,
        onCommit = {
            fileActionUseCase.assignNewFile(it)
            fileActionUseCase.save(openInputFileNameDialog)
        }
    )

    DestructiveChangeConfirmDialog(
        visibleState = openConfirmDialog,
        titleId = R.string.title_clear_text
    ) {
        fileActionUseCase.setText("")
    }

    val observer = object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_PAUSE) {
                fileActionUseCase.save(openInputFileNameDialog)
            }
        }
    }

    val localLifecycle = LocalLifecycleOwner.current.lifecycle

    val keyboardController = LocalSoftwareKeyboardController.current
    DisposableEffect(key1 = "hide_keyboard") {
        localLifecycle.addObserver(observer)

        onDispose {
            fileActionUseCase.save(openInputFileNameDialog)
            localLifecycle.removeObserver(observer)
            keyboardController?.hide()
        }
    }

    /*TODO if (fileActionUseCase.path.value.isEmpty()) {
        textSetter("")
    }*/
}

@Composable
private fun EditorMenuItem(
    labelId: Int,
    iconId: Int,
    onClick: () -> Unit
) {
    val preferenceApplier = PreferenceApplier(LocalContext.current)
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

/**
 * Clear current file path and reset edit-text.
 */
private fun clearPath() {
    //path = ""
    //clearInput()
    //TODO menuBinding.lastSaved.text = ""
}

/**
 * Insert text to [EditText].
 * @param text insert text
 */
fun insert(text: CharSequence?) {
    //TODO editorInput.insert(binding.editorInput.selectionStart, text)
}
