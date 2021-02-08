/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.tab

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.storage.FilesDir
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class TabThumbnailsTest {

    @InjectMockKs
    private lateinit var tabThumbnails: TabThumbnails

    @MockK
    private lateinit var filesDir: FilesDir

    @RelaxedMockK
    private lateinit var file: File

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { filesDir.assignNewFile(any<String>()) }.returns(file)
        every { filesDir.listFiles() }.returns(arrayOf(file))

        every { file.getName() }.returns("test")
        every { file.exists() }.returns(false)
        every { file.delete() }.returns(false)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testDeleteWithNullInput() {
        tabThumbnails.delete(null)

        verify(exactly = 0) { filesDir.assignNewFile(any<String>()) }
        verify(exactly = 0) { file.exists() }
        verify(exactly = 0) { file.delete() }
    }

    @Test
    fun testDeleteWithBlankInput() {
        tabThumbnails.delete(" ")

        verify(exactly = 0) { filesDir.assignNewFile(any<String>()) }
        verify(exactly = 0) { file.exists() }
        verify(exactly = 0) { file.delete() }
    }

    @Test
    fun testDeleteDoesNotExists() {
        tabThumbnails.delete("test")

        verify(exactly = 1) { filesDir.assignNewFile(any<String>()) }
        verify(exactly = 1) { file.exists() }
        verify(exactly = 0) { file.delete() }
    }

    @Test
    fun testDeleteExists() {
        every { file.exists() }.returns(true)

        tabThumbnails.delete("test")

        verify(exactly = 1) { filesDir.assignNewFile(any<String>()) }
        verify(exactly = 1) { file.exists() }
        verify(exactly = 1) { file.delete() }
    }

    @Test
    fun testDeleteUnusedUnmatched() {
        tabThumbnails.deleteUnused(listOf("unmatched"))

        verify(exactly = 1) { filesDir.listFiles() }
        verify(exactly = 1) { file.getName() }
        verify(exactly = 1) { file.delete() }
    }

    @Test
    fun testDeleteUnusedExcepted() {
        tabThumbnails.deleteUnused(listOf("test"))

        verify(exactly = 1) { filesDir.listFiles() }
        verify(exactly = 1) { file.getName() }
        verify(exactly = 0) { file.delete() }
    }

}