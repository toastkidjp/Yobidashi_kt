/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.webview.usecase

import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RedirectionUseCaseTest {

    @MockK
    private lateinit var uri: Uri

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { uri.host }.returns("app.adjust.com")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testIsTarget() {
        assertTrue(RedirectionUseCase.isTarget(uri))
    }

    @Test
    fun testIsNotTarget() {
        every { uri.host }.returns("www.yahoo.co.jp")
        assertFalse(RedirectionUseCase.isTarget(uri))
    }

}