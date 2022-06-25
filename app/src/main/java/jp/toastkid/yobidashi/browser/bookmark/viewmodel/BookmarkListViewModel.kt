/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.bookmark.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookmarkListViewModel : ViewModel() {

    private val _currentFolder = mutableStateOf(Bookmark.getRootFolderName())

    fun currentFolder() = _currentFolder.value

    fun setCurrentFolder(folderName: String) {
        _currentFolder.value = folderName
    }

    private val _bookmarks = mutableStateListOf<Bookmark>()

    fun bookmarks(): SnapshotStateList<Bookmark> = _bookmarks

    fun replaceBookmarkItems(items: Collection<Bookmark>) {
        _bookmarks.clear()
        _bookmarks.addAll(items)
    }

    fun query(
        bookmarkRepository: BookmarkRepository,
        title: String = Bookmark.getRootFolderName()
    ) {
        setCurrentFolder(title)

        CoroutineScope(Dispatchers.Main).launch {
            val items = withContext(Dispatchers.IO) {
                bookmarkRepository.findByParent(title)
            }
            replaceBookmarkItems(items)
        }
    }

    fun clearItems() {
        _bookmarks.clear()
    }

    fun deleteItem(bookmarkRepository: BookmarkRepository, it: Bookmark) {
        bookmarkRepository.delete(it)
        _bookmarks.remove(it)
    }

}