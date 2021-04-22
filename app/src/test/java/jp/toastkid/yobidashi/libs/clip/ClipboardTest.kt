/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.libs.clip

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ClipboardTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var clipboardManager: ClipboardManager

    @MockK
    private lateinit var clipData: ClipData

    @MockK
    private lateinit var item: ClipData.Item

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.getApplicationContext() }.returns(context)
        every { context.getSystemService(any()) }.returns(clipboardManager)
        every { clipboardManager.setPrimaryClip(any()) }.answers { Unit }
        every { clipboardManager.getPrimaryClip() }.answers { clipData }
        every { clipData.getItemAt(any()) }.returns(item)
        every { item.getText() }.returns("test")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testClip() {
        Clipboard.clip(context, "test")

        verify(exactly = 1) { context.getApplicationContext() }
        verify(exactly = 1) { context.getSystemService(any()) }
        verify(exactly = 1) { clipboardManager.setPrimaryClip(any()) }
        verify(exactly = 0) { clipboardManager.getPrimaryClip() }
        verify(exactly = 0) { clipData.getItemAt(any()) }
        verify(exactly = 0) { item.getText() }
    }

    @Test
    fun testGetPrimary() {
        assertEquals("test", Clipboard.getPrimary(context))

        verify(exactly = 1) { context.getApplicationContext() }
        verify(exactly = 1) { context.getSystemService(any()) }
        verify(exactly = 0) { clipboardManager.setPrimaryClip(any()) }
        verify(exactly = 1) { clipboardManager.getPrimaryClip() }
        verify(exactly = 1) { clipData.getItemAt(any()) }
        verify(exactly = 1) { item.getText() }
    }

}