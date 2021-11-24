/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.storage

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class ExternalFileAssignmentTest {

    @InjectMockKs
    private lateinit var externalFileAssignment: ExternalFileAssignment

    @MockK
    private lateinit var fileFactory: (File?, String) -> File

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var parent: File

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { context.getExternalFilesDir(any()) }.returns(parent)
        every { parent.exists() }.returns(true)
        every { parent.mkdirs() }.returns(true)
        every { fileFactory.invoke(any(), any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testDirectoryExists() {
        externalFileAssignment.invoke(context, "test")

        verify(exactly = 0) { parent.mkdirs() }
        verify(exactly = 1) { fileFactory.invoke(any(), any()) }
    }

    @Test
    fun testCallMkdirs() {
        every { parent.exists() }.returns(false)

        externalFileAssignment.invoke(context, "test")

        verify(exactly = 1) { parent.mkdirs() }
        verify(exactly = 1) { fileFactory.invoke(any(), any()) }
    }

}