/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.view

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.text.format.DateFormat
import android.widget.EditText
import android.widget.ScrollView
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.editor.CursorColorSetter
import jp.toastkid.editor.EditTextFinder
import jp.toastkid.editor.EditorContextMenuInitializer
import jp.toastkid.editor.R
import jp.toastkid.editor.load.LoadFromStorageDialogUi
import jp.toastkid.editor.load.StorageFilesFinder
import jp.toastkid.editor.usecase.FileActionUseCase
import jp.toastkid.editor.usecase.MenuActionInvokerUseCase
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.intent.GetContentIntentFactory
import jp.toastkid.lib.intent.ShareIntentFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.viewmodel.event.content.ShareEvent
import jp.toastkid.lib.viewmodel.event.content.ToBottomEvent
import jp.toastkid.lib.viewmodel.event.content.ToTopEvent
import jp.toastkid.lib.viewmodel.event.finder.FindInPageEvent
import jp.toastkid.libs.speech.SpeechMaker
import jp.toastkid.ui.dialog.ConfirmDialog
import jp.toastkid.ui.dialog.DestructiveChangeConfirmDialog
import jp.toastkid.ui.dialog.InputFileNameDialogUi

@Composable
fun EditorTabUi(path: String?) {
    val context = LocalContext.current as? ComponentActivity ?: return

    val editText = remember { EditText(context) }
    val nestedScrollDispatcher = remember { NestedScrollDispatcher() }

    val contentViewModel = viewModel(ContentViewModel::class.java, context)

    val fileActionUseCase = remember {
        FileActionUseCase(
            context,
            contentViewModel,
            mutableStateOf(path ?: ""),
            { editText.text.toString() },
            { editText.setText(it) },
            { contentViewModel.saveEditorTab(it) }
        )
    }

    val localLifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(key1 = localLifecycleOwner, block = {
        val finder = EditTextFinder(editText)
        contentViewModel.event.collect {
            when (it) {
                is ToTopEvent -> {
                    editText.setSelection(0)
                }
                is ToBottomEvent -> {
                    editText.setSelection(editText.text.length)
                }
                is ShareEvent -> {
                    val title =
                        if (path?.contains("/") == true) path.substring(path.lastIndexOf("/") + 1)
                        else path
                    val content = fileActionUseCase.getText()
                    if (content.isEmpty()) {
                        contentViewModel.snackShort(R.string.error_content_is_empty)
                        return@collect
                    }
                    context.startActivity(ShareIntentFactory().invoke(content, title))
                }
                is FindInPageEvent -> {
                    if (it.upward) {
                        finder.findUp(it.word)
                    } else {
                        finder.findDown(it.word)
                    }
                }
                else -> Unit
            }
        }
    })

    AndroidView(
        factory = {
            EditorContextMenuInitializer().invoke(
                editText,
                MenuActionInvokerUseCase(editText, SpeechMaker(it), contentViewModel)
            )
            editText.setBackgroundColor(Color.Transparent.toArgb())
            val preferenceApplier = PreferenceApplier(context)
            editText.setTextColor(preferenceApplier.editorFontColor())
            editText.setTextSize(Dimension.SP, preferenceApplier.editorFontSize().toFloat())
            editText.typeface = Typeface.MONOSPACE
            editText.hint = context.getString(R.string.hint_editor_input)
            editText.setHintTextColor(preferenceApplier.editorFontColor())
            editText.isNestedScrollingEnabled = true

            val substitute = Color(0xDD81D4FA).toArgb()
            CursorColorSetter().invoke(editText, preferenceApplier.editorCursorColor(substitute))
            editText.highlightColor = preferenceApplier.editorHighlightColor(substitute)
            fileActionUseCase.readCurrentFile()
            val scrollView = ScrollView(editText.context)
            scrollView.addView(editText)
            scrollView.setOnScrollChangeListener { _, scrollX, scrollY, oldScrollX, oldScrollY ->
                nestedScrollDispatcher.dispatchPreScroll(
                    Offset((oldScrollX - scrollX).toFloat(), (oldScrollY - scrollY).toFloat()),
                    NestedScrollSource.Fling
                )
            }
            scrollView.overScrollMode = ScrollView.OVER_SCROLL_NEVER
            scrollView
        },
        modifier = Modifier
            .fillMaxSize()
            .background(Color(PreferenceApplier(context).editorBackgroundColor()))
            .nestedScroll(
                object : NestedScrollConnection {},
                nestedScrollDispatcher
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )

    val dialogState = remember { mutableStateOf(false) }

    ConfirmDialog(
        dialogState,
        context.getString(R.string.confirmation),
        context.getString(R.string.message_confirm_exit)
    ) {
        context.finish()
    }

    BackHandler {
        dialogState.value = true
    }

    val localLifecycle = LocalLifecycleOwner.current.lifecycle

    val openInputFileNameDialog = remember { mutableStateOf(false) }
    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_PAUSE) {
            fileActionUseCase.save(openInputFileNameDialog, false)
        }
    }

    DisposableEffect(key1 = editText) {
        localLifecycle.addObserver(observer)

        onDispose {
            fileActionUseCase.save(openInputFileNameDialog, false)
            localLifecycle.removeObserver(observer)
        }
    }

    contentViewModel.clearOptionMenus()

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(key1 = Unit, block = {
        contentViewModel.showAppBar(coroutineScope)

        contentViewModel.replaceAppBarContent {
            AppBarContent(
                contentViewModel,
                fileActionUseCase
            )
        }
    })
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppBarContent(
    contentViewModel: ContentViewModel,
    fileActionUseCase: FileActionUseCase
) {
    val context = LocalContext.current as? ComponentActivity ?: return

    val openLoadFromStorageDialog = remember { mutableStateOf(false) }
    val openInputFileNameDialog = remember { mutableStateOf(false) }

    val loadAs: ActivityResultLauncher<Intent> =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) {
                return@rememberLauncherForActivityResult
            }

            it.data?.data?.let { uri ->
                fileActionUseCase.readFromFileUri(uri)
                openInputFileNameDialog.value = true
            }
        }

    val openConfirmDialog = remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(72.dp)
            .fillMaxWidth()
            .horizontalScroll(
                rememberScrollState()
            )
    ) {
        EditorMenuItem(R.string.load, R.drawable.ic_load) {
            loadAs.launch(GetContentIntentFactory()("text/plain"))
        }

        EditorMenuItem(R.string.save, R.drawable.ic_save) { fileActionUseCase.save(openInputFileNameDialog) }

        EditorMenuItem(R.string.save_as, R.drawable.ic_save_as) { openInputFileNameDialog.value = true }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight()
                .combinedClickable(
                    true,
                    onClick = { contentViewModel.switchTabList() },
                    onLongClick = { contentViewModel.openNewTab() }
                )
        ) {
            Image(
                painterResource(R.drawable.ic_tab),
                contentDescription = stringResource(id = R.string.tab_list),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary, BlendMode.SrcIn),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(12.dp)
            )
            Text(
                text = contentViewModel.tabCount.value.toString(),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
            )
        }

        EditorMenuItem(R.string.load_from_storage, R.drawable.ic_load) {
            openLoadFromStorageDialog.value = true
        }

        Text(
            text = context.getString(R.string.last_saved) + DateFormat.format(" HH:mm:ss", fileActionUseCase.lastSaved.value),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 14.sp,
            maxLines = 2,
            modifier = Modifier
                .padding(start = 4.dp, end = 4.dp)
                .align(Alignment.CenterVertically)
        )

        Text(
            text = context.getString(R.string.message_character_count, fileActionUseCase.getText().length),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 14.sp,
            maxLines = 2,
            modifier = Modifier
                .padding(start = 4.dp, end = 4.dp)
                .align(Alignment.CenterVertically)
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
            fileActionUseCase.makeNewFileWithName(it, fileActionUseCase, openInputFileNameDialog)
        }
    )

    DestructiveChangeConfirmDialog(
        visibleState = openConfirmDialog,
        titleId = R.string.title_clear_text
    ) {
        fileActionUseCase.setText("")
    }
}

@Composable
private fun EditorMenuItem(
    labelId: Int,
    iconId: Int,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(60.dp)
            .fillMaxHeight()
            .clickable {
                onClick()
            }
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = stringResource(id = labelId),
            tint = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(Modifier.requiredWidth(8.dp))
        Text(
            text = stringResource(id = labelId),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            softWrap = false,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
