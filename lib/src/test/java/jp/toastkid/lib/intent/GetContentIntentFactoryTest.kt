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
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class GetContentIntentFactoryTest {

    @InjectMockKs
    private lateinit var getContentIntentFactory: GetContentIntentFactory

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().setType(any()) }.returns(mockk())
        every { anyConstructed<Intent>().putExtra(any(), any<Boolean>()) }.returns(mockk())
        every { anyConstructed<Intent>().setAction(any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        getContentIntentFactory.invoke("text/plain")

        verify(exactly = 1) { anyConstructed<Intent>().setType(any()) }
        verify(exactly = 1) { anyConstructed<Intent>().putExtra(any(), any<Boolean>()) }
    }

}