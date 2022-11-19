/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.bookmark.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.WorkerThread
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.intent.CreateDocumentIntentFactory
import jp.toastkid.lib.intent.GetContentIntentFactory
import jp.toastkid.lib.intent.ShareIntentFactory
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.view.list.SwipeToDismissItem
import jp.toastkid.lib.view.scroll.usecase.ScrollerUseCase
import jp.toastkid.ui.dialog.DestructiveChangeConfirmDialog
import jp.toastkid.ui.dialog.InputFileNameDialogUi
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.FaviconApplier
import jp.toastkid.yobidashi.browser.bookmark.BookmarkInitializer
import jp.toastkid.yobidashi.browser.bookmark.BookmarkInsertion
import jp.toastkid.yobidashi.browser.bookmark.ExportedFileParser
import jp.toastkid.yobidashi.browser.bookmark.Exporter
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import jp.toastkid.yobidashi.browser.bookmark.viewmodel.BookmarkListViewModel
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import timber.log.Timber
import java.io.IOException
import java.util.Stack

private const val EXPORT_FILE_NAME = "bookmark.html"

@Composable
fun BookmarkListUi() {
    val activityContext = LocalContext.current as? ComponentActivity ?: return

    val bookmarkRepository = DatabaseFinder().invoke(activityContext).bookmarkRepository()

    val browserViewModel = viewModel(BrowserViewModel::class.java, activityContext)

    val viewModel = viewModel(BookmarkListViewModel::class.java)

    val folderHistory: Stack<String> = Stack()

    val onClick: (Bookmark, Boolean) -> Unit = { bookmark, isLongClick ->
        when {
            isLongClick -> {
                browserViewModel
                    .openBackground(
                        bookmark.title,
                        Uri.parse(bookmark.url)
                    )
            }
            bookmark.folder -> {
                folderHistory.push(bookmark.parent)
                viewModel.query(bookmarkRepository, bookmark.title)
            }
            else -> {
                browserViewModel.open(Uri.parse(bookmark.url))
            }
        }
    }

    val contentViewModel = viewModel(ContentViewModel::class.java, activityContext)
    val listState = rememberLazyListState()

    BookmarkList(listState, onClick) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                viewModel.deleteItem(bookmarkRepository, it)
            }
        } catch (e: IOException) {
            Timber.e(e)
        }
    }
    ScrollerUseCase(contentViewModel, listState).invoke(LocalLifecycleOwner.current)

    val openClearDialogState = remember { mutableStateOf(false) }
    val openAddFolderDialogState = remember { mutableStateOf(false) }

    val getContentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data == null || it.resultCode != Activity.RESULT_OK) {
                return@rememberLauncherForActivityResult
            }

            val uri = it.data?.data ?: return@rememberLauncherForActivityResult
            importBookmark(activityContext, bookmarkRepository, uri) {
                viewModel.query(bookmarkRepository)
            }
        }

    val importRequestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                contentViewModel.snackShort(R.string.message_requires_permission_storage)
                return@rememberLauncherForActivityResult
            }

            getContentLauncher.launch(GetContentIntentFactory()("text/html"))
        }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.data == null || it.resultCode != Activity.RESULT_OK) {
            return@rememberLauncherForActivityResult
        }

        val uri = it.data?.data ?: return@rememberLauncherForActivityResult
        CoroutineScope(Dispatchers.IO).launch {
            exportBookmark(activityContext, bookmarkRepository, uri)
        }
    }

    val exportRequestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                contentViewModel.snackShort(R.string.message_requires_permission_storage)
                return@rememberLauncherForActivityResult
            }

            exportLauncher.launch(
                CreateDocumentIntentFactory()("text/html", EXPORT_FILE_NAME)
            )
        }

    InputFileNameDialogUi(openAddFolderDialogState, onCommit = { title ->
        CoroutineScope(Dispatchers.Main).launch {
            val currentFolderName =
                if (viewModel.bookmarks().isEmpty() && folderHistory.isNotEmpty()) folderHistory.peek()
                else if (viewModel.bookmarks().isEmpty()) Bookmark.getRootFolderName()
                else viewModel.bookmarks()[0].parent
            BookmarkInsertion(
                activityContext,
                title,
                parent = currentFolderName,
                folder = true
            ).insert()

            viewModel.query(bookmarkRepository)
        }
    })

    DestructiveChangeConfirmDialog(
        openClearDialogState,
        R.string.title_clear_bookmark
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) { bookmarkRepository.clear() }

            contentViewModel.snackShort(R.string.done_clear)
            viewModel.clearItems()
        }
    }

    BackHandler(viewModel.currentFolder() != Bookmark.getRootFolderName()) {
        viewModel.query(bookmarkRepository)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(key1 = "first_launch", block = {
        viewModel.query(bookmarkRepository)

        contentViewModel.optionMenus(
            OptionMenu(titleId = R.string.title_add_folder, action = {
                openAddFolderDialogState.value = true
            }),
            OptionMenu(titleId = R.string.title_import_bookmark, action = {
                importRequestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }),
            OptionMenu(titleId = R.string.title_export_bookmark, action = {
                exportRequestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }),
            OptionMenu(titleId = R.string.title_add_default_bookmark, action = {
                BookmarkInitializer.from(activityContext)() { viewModel.query(bookmarkRepository) }
                contentViewModel.snackShort(R.string.done_addition)
            }),
            OptionMenu(titleId = R.string.title_clear_bookmark, action = {
                openClearDialogState.value = true
            })
        )

        contentViewModel.share.observe(lifecycleOwner) {
            it.getContentIfNotHandled() ?: return@observe

            contentViewModel.viewModelScope.launch {
                val items = withContext(Dispatchers.IO) {
                    bookmarkRepository.all()
                }
                val html = withContext(Dispatchers.IO) {
                    Exporter(items).invoke()
                }

                activityContext.startActivity(
                    ShareIntentFactory()(html, EXPORT_FILE_NAME)
                )
            }

        }
    })

    DisposableEffect(key1 = lifecycleOwner, effect = {
        onDispose {
            contentViewModel.share.removeObservers(lifecycleOwner)
        }
    })
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookmarkList(
    listState: LazyListState,
    onClick: (Bookmark, Boolean) -> Unit,
    onDelete: (Bookmark) -> Unit
) {
    val viewModel = viewModel(BookmarkListViewModel::class.java)
    LazyColumn(
        contentPadding = PaddingValues(bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        state = listState,
        modifier = Modifier.padding(start = 8.dp, end = 8.dp)
    ) {
        items(viewModel.bookmarks(), { it._id }) { bookmark ->
            val openEditor = remember { mutableStateOf(false) }

            SwipeToDismissItem(
                onClickDelete = {
                    onDelete(bookmark)
                },
                dismissContent = {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .combinedClickable(
                                true,
                                onClick = {
                                    onClick(bookmark, false)
                                },
                                onLongClick = {
                                    onClick(bookmark, true)
                                }
                            )
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .animateItemPlacement()
                    ) {
                        AsyncImage(
                            bookmark.favicon,
                            bookmark.title,
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.Center,
                            placeholder = painterResource(id = if (bookmark.folder) R.drawable.ic_folder_black else R.drawable.ic_bookmark),
                            error = painterResource(id = if (bookmark.folder) R.drawable.ic_folder_black else R.drawable.ic_bookmark),
                            modifier = Modifier
                                .width(44.dp)
                                .padding(8.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bookmark.title,
                                fontSize = 18.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (bookmark.url.isNotBlank()) {
                                Text(
                                    text = bookmark.url,
                                    color = colorResource(id = R.color.link_blue),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (bookmark.lastViewed != 0L) {
                                Text(
                                    text = DateFormat.format(
                                        stringResource(R.string.date_format),
                                        bookmark.lastViewed
                                    ).toString(),
                                    color = colorResource(id = R.color.gray_500_dd),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (bookmark.folder.not()) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_option_menu),
                                contentDescription = stringResource(id = R.string.title_option_menu),
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.clickable {
                                    openEditor.value = true
                                }
                            )
                        }
                    }
                },
                modifier = Modifier.animateItemPlacement()
            )

            if (openEditor.value) {
                EditorDialog(openEditor, bookmark)
            }
        }
    }

}

@Composable
private fun EditorDialog(
    openEditor: MutableState<Boolean>,
    currentItem: Bookmark
) {
    val viewModel = viewModel(BookmarkListViewModel::class.java)
    val bookmarkRepository = DatabaseFinder().invoke(LocalContext.current).bookmarkRepository()
    val folders = remember { mutableStateListOf<String>() }
    val moveTo = remember { mutableStateOf(currentItem.parent) }

    LaunchedEffect("load_folders") {
        folders.clear()
        folders.add(Bookmark.getRootFolderName())
        CoroutineScope(Dispatchers.IO).launch {
            folders.addAll(bookmarkRepository.folders())
        }
    }

    Dialog(onDismissRequest = { openEditor.value = false }) {
        Surface(shadowElevation = 4.dp) {
            Box(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .clickable {
                                openEditor.value = false
                            }
                            .padding(16.dp)
                    )

                    Text(
                        text = stringResource(id = R.string.ok),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .clickable {
                                openEditor.value = false
                                if (currentItem.parent != moveTo.value) {
                                    moveFolder(
                                        currentItem,
                                        moveTo.value,
                                        bookmarkRepository,
                                        viewModel
                                    )
                                }
                            }
                            .padding(16.dp)
                    )
                }

                val openChooser = remember { mutableStateOf(false) }

                Column {
                     Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painterResource(id = R.drawable.ic_folder_black),
                                stringResource(id = R.string.title_add_folder)
                            )
                            Text(
                                "Move to other folder",
                                fontSize = 20.sp
                            )
                        }
                    Box(
                        Modifier
                            .padding(start = 4.dp, bottom = 60.dp)
                            .defaultMinSize(200.dp)
                            .background(MaterialTheme.colorScheme.onSurface)
                            .clickable { openChooser.value = true }
                    ) {
                        Text(
                            moveTo.value,
                            color = MaterialTheme.colorScheme.surface,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )

                        DropdownMenu(
                            openChooser.value,
                            onDismissRequest = { openChooser.value = false }
                        ) {
                            folders.forEach {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            it,
                                            fontSize = 20.sp
                                        )
                                    },
                                    onClick = {
                                        openChooser.value = false
                                        moveTo.value = it
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun moveFolder(
    currentItem: Bookmark,
    newFolder: String,
    bookmarkRepository: BookmarkRepository,
    viewModel: BookmarkListViewModel
) {
    val folder = currentItem.parent
    CoroutineScope(Dispatchers.IO).launch {
        currentItem.parent = newFolder
        bookmarkRepository.add(currentItem)
        viewModel.query(bookmarkRepository, folder)
    }
}

/**
 * Import bookmark from selected HTML file.
 *
 * @param uri Bookmark exported html's Uri.
 */
private fun importBookmark(
    context: Context,
    bookmarkRepository: BookmarkRepository,
    uri: Uri,
    showRoot: () -> Unit
) {
    val inputStream = context.contentResolver?.openInputStream(uri) ?: return

    CoroutineScope(Dispatchers.Main).launch {
        val faviconApplier = FaviconApplier(context)
        withContext(Dispatchers.IO) {
            ExportedFileParser()(inputStream)
                .map {
                    it.favicon = faviconApplier.makePath(it.url)
                    it
                }
                .forEach { bookmarkRepository.add(it) }
        }

        showRoot()
    }
}

/**
 * Export bookmark.
 *
 * @param uri
 */
@WorkerThread
private fun exportBookmark(
    context: Context,
    bookmarkRepository: BookmarkRepository,
    uri: Uri
) {
    val items = bookmarkRepository.all()
    context.contentResolver?.openOutputStream(uri)?.use { stream ->
        stream.sink().buffer().use {
            it.writeUtf8(Exporter(items).invoke())
        }
    }
}