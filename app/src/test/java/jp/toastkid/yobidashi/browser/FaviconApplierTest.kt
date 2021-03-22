/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.storage.FilesDir
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class FaviconApplierTest {

    private lateinit var faviconApplier: FaviconApplier

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var filesDirFactory: (Context, String) -> FilesDir

    @MockK(relaxed = true)
    private lateinit var filesDir: FilesDir

    @MockK(relaxed = true)
    private lateinit var file: File

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { filesDirFactory.invoke(any(), any()) }.returns(filesDir)
        every { filesDir.assignNewFile(any<String>()) }.returns(file)

        faviconApplier = FaviconApplier(context, filesDirFactory)

        mockkStatic(BitmapFactory::class)
        every { BitmapFactory.decodeFile(any()) }.returns(mockk())

        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.returns(mockk(relaxed = true))
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun assignFile() {
        faviconApplier.assignFile("test")

        verify(exactly = 1) { filesDirFactory.invoke(any(), any()) }
        verify(exactly = 1) { filesDir.assignNewFile(any<String>()) }
        verify(exactly = 0) { BitmapFactory.decodeFile(any()) }
        verify(exactly = 1) { Uri.parse(any()) }
    }

    @Test
    fun makePath() {
        faviconApplier.makePath("test")

        verify(exactly = 1) { filesDirFactory.invoke(any(), any()) }
        verify(exactly = 1) { filesDir.assignNewFile(any<String>()) }
        verify(exactly = 0) { BitmapFactory.decodeFile(any()) }
        verify(exactly = 1) { Uri.parse(any()) }
    }

    @Test
    fun load() {
        val uri = mockk<Uri>()
        every { uri.toString() }.returns("test")

        faviconApplier.load(uri)

        verify(exactly = 1) { uri.toString() }
        verify(exactly = 1) { filesDirFactory.invoke(any(), any()) }
        verify(exactly = 1) { filesDir.assignNewFile(any<String>()) }
        verify(exactly = 1) { BitmapFactory.decodeFile(any()) }
        verify(exactly = 1) { Uri.parse(any()) }
    }

}