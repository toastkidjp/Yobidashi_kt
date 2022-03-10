/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.bookmark

import android.content.Context
import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.storage.FilesDir
import jp.toastkid.yobidashi.browser.FaviconFolderProviderService
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import jp.toastkid.yobidashi.browser.icon.WebClipIconLoader
import jp.toastkid.yobidashi.libs.db.AppDatabase
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class BookmarkInitializerTest {

    private lateinit var bookmarkInitializer: BookmarkInitializer

    @MockK
    private lateinit var favicons: FilesDir

    @MockK
    private lateinit var webClipIconLoader: WebClipIconLoader

    @MockK
    private lateinit var databaseFinder: DatabaseFinder

    @MockK
    private lateinit var appDatabase: AppDatabase

    @MockK
    private lateinit var bookmarkRepository: BookmarkRepository

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var onComplete: () -> Unit

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { databaseFinder.invoke(any()) }.returns(appDatabase)
        every { appDatabase.bookmarkRepository() }.returns(bookmarkRepository)
        coEvery { bookmarkRepository.add(any()) }.just(Runs)
        coEvery { onComplete.invoke() }.just(Runs)

        val file = mockk<File>()
        coEvery { file.getAbsolutePath() }.returns("/test/test")
        coEvery { favicons.assignNewFile(any<String>()) }.returns(file)

        val uri = mockk<Uri>()
        coEvery { uri.getHost() }.returns("test")
        mockkStatic(Uri::class)
        coEvery { Uri.parse(any()) }.returns(uri)

        bookmarkInitializer = BookmarkInitializer(
            bookmarkRepository, favicons, webClipIconLoader, Dispatchers.Unconfined, Dispatchers.Unconfined
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun from() {
        mockkConstructor(FaviconFolderProviderService::class)
        every { anyConstructed<FaviconFolderProviderService>().invoke(any()) }.returns(mockk())
        mockkObject(WebClipIconLoader)
        every { WebClipIconLoader.from(any()) }.returns(mockk())

        BookmarkInitializer.from(context)

        verify(exactly = 1) { databaseFinder.invoke(any()) }
        verify(exactly = 1) { appDatabase.bookmarkRepository() }
        verify { anyConstructed<FaviconFolderProviderService>().invoke(any()) }
        verify { WebClipIconLoader.from(any()) }
    }

    @Test
    fun invoke() {
        bookmarkInitializer.invoke(context, onComplete)

        coVerify(atLeast = 1) { bookmarkRepository.add(any()) }
        coVerify(atLeast = 1) { favicons.assignNewFile(any<String>()) }
    }

}