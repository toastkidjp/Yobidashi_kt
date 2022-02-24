/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.view.list

import androidx.recyclerview.widget.DiffUtil
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CommonItemCallbackTest {

    private lateinit var itemCallback: DiffUtil.ItemCallback<String>

    @MockK
    private lateinit var sameItemComparator: (String, String) -> Boolean

    @MockK
    private lateinit var equals: (String, String) -> Boolean

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { sameItemComparator.invoke(any(), any()) }.returns(true)
        every { equals.invoke(any(), any()) }.returns(true)

        itemCallback = CommonItemCallback.with(sameItemComparator, equals)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testAreItemsTheSame() {
        assertTrue(itemCallback.areItemsTheSame("test", "test"))

        verify { sameItemComparator.invoke(any(), any()) }
    }

    @Test
    fun testAreContentsTheSame() {
        itemCallback.areContentsTheSame("test", "test")

        verify { equals.invoke(any(), any()) }
    }
}