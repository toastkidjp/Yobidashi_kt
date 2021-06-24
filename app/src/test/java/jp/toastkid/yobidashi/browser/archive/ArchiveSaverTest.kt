/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.archive

import android.webkit.WebView
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * @author toastkidjp
 */
class ArchiveSaverTest {

    private lateinit var archiveSaver: ArchiveSaver

    @MockK
    private lateinit var webView: WebView

    @MockK
    private lateinit var file: File

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { webView.saveWebArchive(any(), any(), any()) }.returns(Unit)
        every { file.getAbsolutePath() }.returns("/path/to/file")

        archiveSaver = ArchiveSaver()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        archiveSaver.invoke(webView, file)

        verify(exactly = 1) { webView.saveWebArchive(any(), any(), any()) }
        verify(exactly = 1) { file.getAbsolutePath() }
    }

}