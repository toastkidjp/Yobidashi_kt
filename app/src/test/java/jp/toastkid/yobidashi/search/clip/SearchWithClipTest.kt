/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.clip

import android.content.ClipboardManager
import android.view.View
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.preference.ColorPair
import org.junit.After
import org.junit.Before
import org.junit.Test

class SearchWithClipTest {

    @InjectMockKs
    private lateinit var searchWithClip: SearchWithClip

    @MockK
    private lateinit var cm: ClipboardManager

    @MockK
    private lateinit var parent: View

    @MockK
    private lateinit var colorPair: ColorPair

    @MockK
    private lateinit var browserViewModel: BrowserViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { cm.addPrimaryClipChangedListener(any()) }.answers { Unit }
        every { cm.removePrimaryClipChangedListener(any()) }.answers { Unit }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        searchWithClip.invoke()

        verify(exactly = 1) { cm.addPrimaryClipChangedListener(any()) }
    }

    @Test
    fun testDispose() {
        searchWithClip.dispose()

        verify(exactly = 1) { cm.removePrimaryClipChangedListener(any()) }
    }

}