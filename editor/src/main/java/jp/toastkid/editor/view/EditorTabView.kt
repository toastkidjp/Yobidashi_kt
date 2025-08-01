/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.editor.R
import jp.toastkid.editor.load.LoadFromStorageDialogUi
import jp.toastkid.editor.load.StorageFilesFinder
import jp.toastkid.editor.usecase.FileActionUseCase
import jp.toastkid.editor.view.menu.EditorMenuInjector
import jp.toastkid.editor.view.menu.MenuActionInvoker
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.intent.GetContentIntentFactory
import jp.toastkid.lib.intent.ShareIntentFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.viewmodel.event.content.ShareEvent
import jp.toastkid.lib.viewmodel.event.content.ToBottomEvent
import jp.toastkid.lib.viewmodel.event.content.ToTopEvent
import jp.toastkid.lib.viewmodel.event.finder.FindInPageEvent
import jp.toastkid.ui.dialog.ConfirmDialog
import jp.toastkid.ui.dialog.DestructiveChangeConfirmDialog
import jp.toastkid.ui.dialog.InputFileNameDialogUi
import jp.toastkid.ui.menu.context.ContextMenuToolbar
import kotlinx.coroutines.launch

@Composable
fun EditorTabView(path: String?, modifier: Modifier) {
    val context = LocalContext.current as? ComponentActivity ?: return

    val contentViewModel = viewModel(ContentViewModel::class.java, context)

    val viewModel = remember { EditorTabViewModel() }

    val coroutineScope = rememberCoroutineScope()

    val fileActionUseCase = remember {
        FileActionUseCase(
            context,
            contentViewModel,
            mutableStateOf(path ?: ""),
            { viewModel.content().text },
            { viewModel.onValueChange(viewModel.content().copy(text = it)) },
            contentViewModel::saveEditorTab,
            viewModel::setLastSaved
        )
    }

    LaunchedEffect(key1 = LocalLifecycleOwner.current, block = {
        contentViewModel.event.collect {
            when (it) {
                is ToTopEvent -> viewModel.scrollToTop()
                is ToBottomEvent -> viewModel.scrollToBottom()
                is ShareEvent -> {
                    val title =
                        if (path?.contains("/") == true) path.substring(path.lastIndexOf("/") + 1)
                        else path
                    val content = viewModel.content().text
                    if (content.isEmpty()) {
                        contentViewModel.snackShort(R.string.error_content_is_empty)
                        return@collect
                    }
                    context.startActivity(ShareIntentFactory().invoke(content, title))
                }
                is FindInPageEvent -> {
                    if (it.upward) {
                        viewModel.findUp(it.word)
                    } else {
                        viewModel.findDown(it.word)
                    }
                }
                else -> Unit
            }
        }
    })

    CompositionLocalProvider(
        LocalTextToolbar provides ContextMenuToolbar(
            LocalView.current,
            EditorMenuInjector(context, viewModel::selectedText),
            MenuActionInvoker(viewModel, context, contentViewModel)
        )
    ) {
        BasicTextField(
            value = viewModel.content(),
            onValueChange = viewModel::onValueChange,
            onTextLayout = {
                viewModel.setMultiParagraph(it.multiParagraph)
            },
            visualTransformation = viewModel.visualTransformation(),
            decorationBox = {
                Row {
                    Column(
                        modifier = Modifier
                            .verticalScroll(viewModel.lineNumberScrollState())
                            .padding(horizontal = 4.dp)
                            .wrapContentSize(unbounded = true)
                    ) {
                        viewModel.lineNumbers().forEach { (lineNumber, lineNumberText) ->
                            Box(
                                contentAlignment = Alignment.CenterEnd,
                                modifier = Modifier
                                    .clickable {
                                        viewModel.onClickLineNumber(lineNumber)
                                    }
                                    .semantics {
                                        contentDescription = "Line number $lineNumberText"
                                    }
                            ) {
                                Text(
                                    lineNumberText,
                                    color = viewModel.fontColor(),
                                    fontSize = viewModel.fontSize(),
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.End,
                                    style = TextStyle(
                                        lineHeight = 1.5.em,
                                        lineHeightStyle = LineHeightStyle(
                                            alignment = LineHeightStyle.Alignment.Center,
                                            trim = LineHeightStyle.Trim.None
                                        )
                                    )
                                )
                            }
                        }
                    }
                    VerticalDivider()
                    it()
                }
            },
            textStyle = TextStyle(
                color = viewModel.fontColor(),
                fontSize = viewModel.fontSize(),
                fontFamily = FontFamily.Monospace,
                lineHeight = 1.5.em,
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.None
                ),
                background = Color.Transparent
            ),
            cursorBrush = SolidColor(viewModel.cursorColor()),
            modifier = modifier
                .focusRequester(viewModel.focusRequester())
                .fillMaxWidth()
                .drawBehind {
                    drawRect(viewModel.backgroundColor())

                    val currentLineOffset = viewModel.currentLineOffset()
                    if (currentLineOffset != Offset.Unspecified) {
                        drawRect(
                            viewModel.currentLineHighlightColor(),
                            topLeft = currentLineOffset,
                            size = Size(Float.MAX_VALUE, 24.sp.toPx())
                        )
                    }
                }
                .semantics { contentDescription = "Editor input area" }
                .nestedScroll(
                    object : NestedScrollConnection {
                        override fun onPreScroll(
                            available: Offset,
                            source: NestedScrollSource
                        ): Offset {
                            coroutineScope.launch {
                                viewModel
                                    .lineNumberScrollState()
                                    .scrollBy(-available.y)
                            }
                            return super.onPreScroll(available, source)
                        }
                    },
                    viewModel.nestedScrollDispatcher()
                )
                .padding(vertical = 2.dp)
                .padding(start = 2.dp, end = 4.dp)
        )
    }

    if (viewModel.isOpenExitDialog()) {
        ConfirmDialog(
            stringResource(jp.toastkid.lib.R.string.confirmation),
            stringResource(jp.toastkid.lib.R.string.message_confirm_exit),
            onDismissRequest = viewModel::closeExitDialog,
            onClickOk = context::finish
        )
    }

    BackHandler {
        viewModel.openExitDialog()
    }

    val localLifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(key1 = path) {
        viewModel.launchTab(TextFieldValue())
        fileActionUseCase.readCurrentFile()
        viewModel.initialScroll(coroutineScope)

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                fileActionUseCase.save(viewModel::openInputFileNameDialog, false)
            }
        }

        localLifecycle.addObserver(observer)

        onDispose {
            fileActionUseCase.save(viewModel::openInputFileNameDialog, false)
            localLifecycle.removeObserver(observer)
            viewModel.dispose()
        }

    }

    if (viewModel.isOpenConfirmDialog()) {
        DestructiveChangeConfirmDialog(
            titleId = R.string.title_clear_text,
            onDismissRequest = viewModel::closeConfirmDialog,
            onClickOk = viewModel::clearText
        )
    }

    if (viewModel.isOpenLoadFromStorageDialog()) {
        LoadFromStorageDialogUi(
            files = StorageFilesFinder().invoke(context),
            onDismissRequest = viewModel::closeLoadFromStorageDialog,
            onSelect = { fileActionUseCase.readFromFileUri(Uri.fromFile(it)) }
        )
    }

    if (viewModel.isOpenInputFileNameDialog()) {
        InputFileNameDialogUi(
            onCommit = { fileActionUseCase.makeNewFileWithName(it, viewModel::openInputFileNameDialog) },
            onDismissRequest = viewModel::closeInputFileNameDialog
        )
    }

    LaunchedEffect(key1 = Unit, block = {
        viewModel.setPreference(PreferenceApplier(context))

        contentViewModel.clearOptionMenus()
        contentViewModel.showAppBar(coroutineScope)

        contentViewModel.replaceAppBarContent {
            AppBarContent(
                viewModel.lastSaved(),
                viewModel.contentLength(),
                { fileActionUseCase.save(viewModel::openInputFileNameDialog) },
                viewModel::openConfirmDialog,
                viewModel::openInputFileNameDialog,
                viewModel::openLoadFromStorageDialog,
                fileActionUseCase,
                contentViewModel.tabCount.value,
                contentViewModel::switchTabList,
                contentViewModel::openNewTab
            )
        }
    })
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppBarContent(
    lastSaved: CharSequence,
    contentLength: Int,
    saveFile: () -> Unit,
    openConfirmDialog: () -> Unit,
    openInputFileNameDialog: () -> Unit,
    openLoadFromStorageDialog: () -> Unit,
    fileActionUseCase: FileActionUseCase,
    tabCount: Int,
    switchTabList: () -> Unit,
    openNewTab: () -> Unit
) {
    val loadAs: ActivityResultLauncher<Intent> =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) {
                return@rememberLauncherForActivityResult
            }

            it.data?.data?.let { uri ->
                fileActionUseCase.readFromFileUri(uri)
                openInputFileNameDialog()
            }
        }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(72.dp)
            .fillMaxWidth()
            .horizontalScroll(
                rememberScrollState()
            )
    ) {
        EditorMenuItem(jp.toastkid.lib.R.string.load, R.drawable.ic_load) {
            loadAs.launch(GetContentIntentFactory()("text/plain"))
        }

        EditorMenuItem(
            jp.toastkid.lib.R.string.save,
            R.drawable.ic_save, saveFile
        )

        EditorMenuItem(
            R.string.save_as, R.drawable.ic_save_as,
            openInputFileNameDialog
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight()
                .combinedClickable(
                    true,
                    onClick = switchTabList,
                    onLongClick = openNewTab
                )
        ) {
            Image(
                painterResource(jp.toastkid.lib.R.drawable.ic_tab),
                contentDescription = stringResource(id = jp.toastkid.lib.R.string.tab_list),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary, BlendMode.SrcIn),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(12.dp)
            )
            Text(
                text = "$tabCount",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
            )
        }

        EditorMenuItem(
            R.string.load_from_storage,
            R.drawable.ic_load,
            openLoadFromStorageDialog
        )

        LastSaved(
            lastSaved,
            Modifier
                .padding(start = 4.dp, end = 4.dp)
                .align(Alignment.CenterVertically)
        )

        ContentLength(
            contentLength,
            Modifier
                .padding(start = 4.dp, end = 4.dp)
                .align(Alignment.CenterVertically)
        )

        EditorMenuItem(jp.toastkid.lib.R.string.clear_all, jp.toastkid.lib.R.drawable.ic_clear_form, openConfirmDialog)
    }
}

@Composable
private fun LastSaved(lastSaved: CharSequence, modifier: Modifier) {
    Text(
        text = stringResource(R.string.last_saved) + lastSaved,
        color = MaterialTheme.colorScheme.onPrimary,
        fontSize = 14.sp,
        maxLines = 2,
        modifier = modifier
    )
}

@Composable
private fun ContentLength(contentLength: Int, modifier: Modifier) {
    Text(
        text = stringResource(jp.toastkid.lib.R.string.message_character_count, contentLength),
        color = MaterialTheme.colorScheme.onPrimary,
        fontSize = 14.sp,
        maxLines = 2,
        modifier = modifier
    )
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
            .clickable(onClick = onClick)
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
