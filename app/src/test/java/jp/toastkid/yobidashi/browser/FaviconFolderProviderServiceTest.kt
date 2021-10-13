/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import jp.toastkid.lib.storage.FilesDir
import org.junit.Before
import org.junit.Test

class FaviconFolderProviderServiceTest {

    @InjectMockKs
    private lateinit var faviconFolderProviderService: FaviconFolderProviderService

    @MockK
    private lateinit var filesDirProvider: (Context) -> FilesDir

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { filesDirProvider.invoke(any()) }.returns(mockk())
    }

    @Test
    fun testInvoke() {
        faviconFolderProviderService.invoke(mockk())

        verify(exactly = 1) { filesDirProvider.invoke(any()) }
    }

}