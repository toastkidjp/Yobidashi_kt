/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.libs

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class DateFormatHolderTest {

    @MockK
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { context.getString(any()) }.returns("E, MM/dd/yyyy HH:mm:ss")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        DateFormatHolder.invoke(context)

        verify(exactly = 1) { context.getString(any()) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testIllegalArgumentCase() {
        every { context.getString(any()) }.returns("test")

        DateFormatHolder.invoke(context)

        verify(exactly = 1) { context.getString(any()) }
    }

}