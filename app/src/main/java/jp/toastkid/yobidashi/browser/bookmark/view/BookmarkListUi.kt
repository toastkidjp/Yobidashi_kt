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
import androidx.compose.material.DismissState
import androidx.compose.material.DismissValue
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.color.IconColorFinder
import jp.toastkid.lib.intent.CreateDocumentIntentFactory
import jp.toastkid.lib.intent.GetContentIntentFactory
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.view.scroll.usecase.ScrollerUseCase
import jp.toastkid.ui.dialog.DestructiveChangeConfirmDialog
import jp.toastkid.ui.dialog.InputFileNameDialogUi
import jp.toastkid.ui.list.SwipeToDismissItem
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.FaviconApplier
import jp.toastkid.yobidashi.browser.bookmark.BookmarkInitializer
import jp.toastkid.yobidashi.browser.bookmark.BookmarkInsertion
import jp.toastkid.yobidashi.browser.bookmark.ExportedFileParser
import jp.toastkid.yobidashi.browser.bookmark.Exporter
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
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

private var currentFolder: String = Bookmark.getRootFolderName()

@Composable
fun BookmarkListUi() {
    val activityContext = LocalContext.current as? ComponentActivity ?: return

    val bookmarkRepository = DatabaseFinder().invoke(activityContext).bookmarkRepository()
    val bookmarks = remember { mutableStateListOf<Bookmark>() }

    val browserViewModel = ViewModelProvider(activityContext).get(BrowserViewModel::class.java)

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
                query(bookmarkRepository, bookmarks, bookmark.title)
            }
            else -> {
                browserViewModel.openBackground(bookmark.title, Uri.parse(bookmark.url))
            }
        }
    }

    val contentViewModel = ViewModelProvider(activityContext).get(ContentViewModel::class.java)
    val listState = rememberLazyListState()

    BookmarkList(bookmarks, listState, onClick) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                bookmarkRepository.delete(it)
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
                query(bookmarkRepository, bookmarks)
            }
        }

    val importRequestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                contentViewModel?.snackShort(R.string.message_requires_permission_storage)
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
                contentViewModel?.snackShort(R.string.message_requires_permission_storage)
                return@rememberLauncherForActivityResult
            }

            exportLauncher.launch(
                CreateDocumentIntentFactory()("text/html", "bookmark.html")
            )
        }

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
            BookmarkInitializer.from(activityContext)() { query(bookmarkRepository, bookmarks) }
            contentViewModel.snackShort(R.string.done_addition)
        }),
        OptionMenu(titleId = R.string.title_clear_bookmark, action = {
            openClearDialogState.value = true
        })
    )

    InputFileNameDialogUi(openAddFolderDialogState, onCommit = { title ->
        CoroutineScope(Dispatchers.Main).launch {
            val currentFolderName =
                if (bookmarks.isEmpty() && folderHistory.isNotEmpty()) folderHistory.peek()
                else if (bookmarks.isEmpty()) Bookmark.getRootFolderName()
                else bookmarks[0].parent
            BookmarkInsertion(
                activityContext,
                title,
                parent = currentFolderName,
                folder = true
            ).insert()

            query(bookmarkRepository, bookmarks)
        }
    })

    DestructiveChangeConfirmDialog(
        openClearDialogState,
        R.string.title_clear_bookmark
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) { bookmarkRepository.clear() }

            contentViewModel.snackShort(R.string.done_clear)
            bookmarks.clear()
        }
    }

    val backHandlerState = remember { mutableStateOf(true) }
    BackHandler(backHandlerState.value) {
        query(bookmarkRepository, bookmarks)
        backHandlerState.value = currentFolder != Bookmark.getRootFolderName()
    }

    LaunchedEffect(key1 = "first_launch", block = {
        query(bookmarkRepository, bookmarks)
    })
}

private fun query(
    bookmarkRepository: BookmarkRepository,
    bookmarks: SnapshotStateList<Bookmark>,
    title: String = Bookmark.getRootFolderName()
) {
    currentFolder = title

    CoroutineScope(Dispatchers.Main).launch {
        val items = withContext(Dispatchers.IO) {
            bookmarkRepository.findByParent(title)
        }
        bookmarks.clear()
        bookmarks.addAll(items)
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
private fun BookmarkList(
    bookmarks: SnapshotStateList<Bookmark>,
    listState: LazyListState,
    onClick: (Bookmark, Boolean) -> Unit,
    onDelete: (Bookmark) -> Unit
) {
    val iconColor = Color(IconColorFinder.from(LocalContext.current).invoke())
    LazyColumn(
        contentPadding = PaddingValues(bottom = 4.dp),
        state = listState,
        modifier = Modifier.padding(start = 8.dp, end = 8.dp)
    ) {
        items(bookmarks) { bookmark ->
            val dismissState = DismissState(
                initialValue = DismissValue.Default,
                confirmStateChange = { dismissValue ->
                    if (dismissValue == DismissValue.DismissedToStart) {
                        onDelete(bookmark)
                    }
                    true
                }
            )

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
                                tint = iconColor,
                                modifier = Modifier.clickable {
                                    openEditor.value = true
                                }
                            )
                        }
                    }
                },
                modifier = Modifier
            )

            if (openEditor.value) {
                EditorDialog(openEditor, bookmark, bookmarks)
            }
        }
    }

}

@Composable
private fun EditorDialog(
    openEditor: MutableState<Boolean>,
    currentItem: Bookmark,
    bookmarks: SnapshotStateList<Bookmark>
) {
    val bookmarkRepository = DatabaseFinder().invoke(LocalContext.current).bookmarkRepository()
    val folders = remember { mutableStateListOf<String>() }
    val currentFolder = remember { mutableStateOf(currentItem.parent) }

    LaunchedEffect("load_folders") {
        folders.clear()
        folders.add(Bookmark.getRootFolderName())
        CoroutineScope(Dispatchers.IO).launch {
            folders.addAll(bookmarkRepository.folders())
        }
    }

    Dialog(onDismissRequest = { openEditor.value = false }) {
        Surface(elevation = 4.dp) {
            Box() {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel),
                        color = MaterialTheme.colors.onSurface,
                        modifier = Modifier
                            .clickable {
                                openEditor.value = false
                            }
                            .padding(16.dp)
                    )

                    Text(
                        text = stringResource(id = R.string.ok),
                        color = MaterialTheme.colors.onSurface,
                        modifier = Modifier
                            .clickable {
                                openEditor.value = false
                                if (currentItem.parent != currentFolder.value) {
                                    moveFolder(
                                        currentItem,
                                        currentFolder.value,
                                        bookmarkRepository,
                                        bookmarks
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
                            .padding(bottom = 60.dp)
                            .defaultMinSize(200.dp)
                            .background(Color(0xDDFFFFFF))
                            .clickable { openChooser.value = true }
                    ) {
                        Text(
                            currentFolder.value,
                            fontSize = 20.sp
                        )

                        DropdownMenu(
                            openChooser.value,
                            onDismissRequest = { openChooser.value = false }
                        ) {
                            folders.forEach {
                                DropdownMenuItem(
                                    onClick = {
                                        openChooser.value = false
                                        currentFolder.value = it
                                    }
                                ) {
                                    Text(
                                        it,
                                        fontSize = 20.sp
                                    )
                                }
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
    bookmarks: SnapshotStateList<Bookmark>
) {
    val folder = currentItem.parent
    CoroutineScope(Dispatchers.IO).launch {
        currentItem.parent = newFolder
        bookmarkRepository.add(currentItem)
        query(bookmarkRepository, bookmarks, folder)
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
    val outputStream = context?.contentResolver?.openOutputStream(uri) ?: return
    outputStream.sink().use { sink ->
        sink.buffer().use {
            it.writeUtf8(Exporter(items).invoke())
        }
    }
}