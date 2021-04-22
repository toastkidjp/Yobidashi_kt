/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.translate

import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.BrowserViewModel
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class TranslatedPageOpenerUseCaseTest {

    @InjectMockKs
    private lateinit var translatedPageOpenerUseCase: TranslatedPageOpenerUseCase

    @MockK
    private lateinit var browserViewModel: BrowserViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { browserViewModel.open(any()) }.returns(Unit)

        mockkStatic(Uri::class)
        every { Uri.encode(any()) }.returns("https://www.yahoo.co.jp")
        every { Uri.parse(any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        translatedPageOpenerUseCase.invoke("https://www.yahoo.co.jp")

        verify(exactly = 1) { browserViewModel.open(any()) }
        verify(exactly = 1) { Uri.encode(any()) }
        verify(exactly = 1) { Uri.parse(any()) }
    }

}