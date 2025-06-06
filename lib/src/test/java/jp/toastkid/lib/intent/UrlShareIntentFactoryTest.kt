/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.intent

import android.content.Intent
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class UrlShareIntentFactoryTest {

    @InjectMockKs
    private lateinit var intentFactory: UrlShareIntentFactory

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().addFlags(any()) }.returns(mockk())
        every { anyConstructed<Intent>().setType(any()) }.returns(mockk())
        every { anyConstructed<Intent>().putExtra(any(), any<String>()) }.returns(mockk())

        mockkStatic(Intent::class)
        every { Intent.createChooser(any(), any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        intentFactory.invoke("https://www.yahoo.co.jp")

        verify(exactly = 1) { anyConstructed<Intent>().type = any() }
        verify(exactly = 1) { anyConstructed<Intent>().addFlags(any()) }
        verify(exactly = 1) { anyConstructed<Intent>().putExtra(Intent.EXTRA_TEXT, any<String>()) }
        verify(exactly = 1) { anyConstructed<Intent>().putExtra(Intent.EXTRA_SUBJECT, any<String>()) }
    }

}