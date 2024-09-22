/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.web.bookmark.view

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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import jp.toastkid.data.repository.factory.RepositoryFactory
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.intent.CreateDocumentIntentFactory
import jp.toastkid.lib.intent.GetContentIntentFactory
import jp.toastkid.lib.intent.ShareIntentFactory
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.view.list.SwipeToDismissItem
import jp.toastkid.lib.view.scroll.usecase.ScrollerUseCase
import jp.toastkid.lib.viewmodel.event.content.ShareEvent
import jp.toastkid.ui.dialog.DestructiveChangeConfirmDialog
import jp.toastkid.ui.dialog.InputFileNameDialogUi
import jp.toastkid.web.FaviconApplier
import jp.toastkid.web.R
import jp.toastkid.web.bookmark.BookmarkInitializer
import jp.toastkid.web.bookmark.BookmarkInsertion
import jp.toastkid.web.bookmark.ExportedFileParser
import jp.toastkid.web.bookmark.Exporter
import jp.toastkid.web.bookmark.viewmodel.BookmarkListViewModel
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.util.Stack

private const val EXPORT_FILE_NAME = "bookmark.html"

@Composable
fun BookmarkListUi() {
    val activityContext = LocalContext.current as? ComponentActivity ?: return

    val bookmarkRepository = remember { RepositoryFactory().bookmarkRepository(activityContext) }

    val contentViewModel1 = viewModel(ContentViewModel::class.java, activityContext)

    val viewModel = remember { BookmarkListViewModel() }

    val folderHistory: Stack<String> = Stack()

    val onClick: (Bookmark, Boolean) -> Unit = { bookmark, isLongClick ->
        when {
            isLongClick -> {
                contentViewModel1
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
                contentViewModel1.open(Uri.parse(bookmark.url))
            }
        }
    }

    val contentViewModel = viewModel(ContentViewModel::class.java, activityContext)
    val listState = rememberLazyListState()

    BookmarkList(viewModel.bookmarks(), listState, onClick, {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                viewModel.deleteItem(bookmarkRepository, it)
            }
        } catch (e: IOException) {
            Timber.e(e)
        }
    },
        {
            viewModel.query(bookmarkRepository, it)
        }
    )
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
                contentViewModel.snackShort(jp.toastkid.lib.R.string.message_requires_permission_storage)
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
                contentViewModel.snackShort(jp.toastkid.lib.R.string.message_requires_permission_storage)
                return@rememberLauncherForActivityResult
            }

            exportLauncher.launch(
                CreateDocumentIntentFactory()("text/html", EXPORT_FILE_NAME)
            )
        }

    if (openAddFolderDialogState.value) {
        InputFileNameDialogUi(
            onDismissRequest = { openAddFolderDialogState.value = false },
            onCommit = { title ->
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
    }

    if (openClearDialogState.value) {
        DestructiveChangeConfirmDialog(
            R.string.title_clear_bookmark,
            onDismissRequest = { openClearDialogState.value = false }
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.IO) { bookmarkRepository.clear() }

                contentViewModel.snackShort(jp.toastkid.lib.R.string.done_clear)
                viewModel.clearItems()
            }
        }
    }

    BackHandler(viewModel.currentFolder() != Bookmark.getRootFolderName()) {
        viewModel.query(bookmarkRepository)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(key1 = lifecycleOwner, block = {
        contentViewModel.event.collect {
            if (it is ShareEvent) {
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
                contentViewModel.snackShort(jp.toastkid.lib.R.string.done_addition)
            }),
            OptionMenu(titleId = R.string.title_clear_bookmark, action = {
                openClearDialogState.value = true
            })
        )
    })
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookmarkList(
    bookmarks: SnapshotStateList<Bookmark>,
    listState: LazyListState,
    onClick: (Bookmark, Boolean) -> Unit,
    onDelete: (Bookmark) -> Unit,
    query: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        state = listState,
        modifier = Modifier.padding(start = 8.dp, end = 8.dp)
    ) {
        item {
            Spacer(modifier = Modifier.padding(vertical = 2.dp))
        }

        items(bookmarks, { it._id }) { bookmark ->
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
                            placeholder = painterResource(id = if (bookmark.folder) R.drawable.ic_folder_black else jp.toastkid.lib.R.drawable.ic_bookmark),
                            error = painterResource(id = if (bookmark.folder) R.drawable.ic_folder_black else jp.toastkid.lib.R.drawable.ic_bookmark),
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
                                    color = colorResource(id = jp.toastkid.lib.R.color.link_blue),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (bookmark.lastViewed != 0L) {
                                Text(
                                    text = DateFormat.format(
                                        stringResource(jp.toastkid.lib.R.string.date_format),
                                        bookmark.lastViewed
                                    ).toString(),
                                    color = Color(0xDD9E9E9E),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (bookmark.folder.not()) {
                            Icon(
                                painter = painterResource(id = jp.toastkid.lib.R.drawable.ic_option_menu),
                                contentDescription = stringResource(id = jp.toastkid.lib.R.string.title_option_menu),
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.clickable {
                                    openEditor.value = true
                                }
                            )
                        }
                    }
                },
                modifier = Modifier.animateItem()
            )

            if (openEditor.value) {
                EditorDialog({ openEditor.value = false }, bookmark, query)
            }
        }
    }

}

@Composable
private fun EditorDialog(
    onDismissRequest: () -> Unit,
    currentItem: Bookmark,
    query: (String) -> Unit
) {
    val bookmarkRepository = RepositoryFactory().bookmarkRepository(LocalContext.current)
    val folders = remember { mutableStateListOf<String>() }
    val moveTo = remember { mutableStateOf(currentItem.parent) }

    LaunchedEffect("load_folders") {
        folders.clear()
        folders.add(Bookmark.getRootFolderName())
        CoroutineScope(Dispatchers.IO).launch {
            folders.addAll(bookmarkRepository.folders())
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(shadowElevation = 4.dp) {
            Box(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text(
                        text = stringResource(id = jp.toastkid.lib.R.string.cancel),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .clickable(onClick = onDismissRequest)
                            .padding(16.dp)
                    )

                    Text(
                        text = stringResource(id = jp.toastkid.lib.R.string.ok),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .clickable {
                                onDismissRequest()

                                if (currentItem.parent != moveTo.value) {
                                    moveFolder(
                                        currentItem,
                                        moveTo.value,
                                        bookmarkRepository,
                                        query
                                    )
                                }
                            }
                            .padding(16.dp)
                    )
                }

                val openChooser = remember { mutableStateOf(false) }

                Column {
                     Row(
                         verticalAlignment = Alignment.CenterVertically,
                         modifier = Modifier.padding(bottom = 8.dp)
                     ) {
                            Icon(
                                painterResource(id = R.drawable.ic_folder_black),
                                stringResource(id = R.string.title_add_folder)
                            )
                            Text(
                                "Move to other folder",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
                    Box(
                        Modifier
                            .padding(start = 4.dp, bottom = 60.dp)
                            .defaultMinSize(200.dp)
                            .drawBehind { drawRect(onSurfaceColor) }
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
    query: (String) -> Unit
) {
    val folder = currentItem.parent
    CoroutineScope(Dispatchers.IO).launch {
        currentItem.parent = newFolder
        bookmarkRepository.add(currentItem)
        query(folder)
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
        stream.bufferedWriter().use {
            it.write(Exporter(items).invoke())
        }
    }
}