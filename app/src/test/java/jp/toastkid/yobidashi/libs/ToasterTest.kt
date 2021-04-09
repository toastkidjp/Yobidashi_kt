/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.libs

import android.widget.Toast
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class ToasterTest {

    @MockK
    private lateinit var toast: Toast

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { toast.show() }.answers { Unit }

        mockkStatic(Toast::class)
        every { Toast.makeText(any(), any<Int>(), any()) }.returns(toast)
        every { Toast.makeText(any(), any<String>(), any()) }.returns(toast)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testToast() {
        Toaster.tShort(mockk(), -1)

        verify(exactly = 1) { Toast.makeText(any(), any<Int>(), any()) }
        verify(exactly = 0) { Toast.makeText(any(), any<String>(), any()) }
        verify(exactly = 1)  { toast.show() }
    }

}