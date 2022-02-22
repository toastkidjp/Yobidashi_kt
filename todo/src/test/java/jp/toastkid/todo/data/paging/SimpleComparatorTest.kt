/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.todo.data.paging

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.unmockkAll
import jp.toastkid.todo.model.TodoTask
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SimpleComparatorTest {

    private lateinit var simpleComparator: SimpleComparator

    @RelaxedMockK
    private lateinit var item0: TodoTask

    @Before
    fun setUp() {
        simpleComparator = SimpleComparator()

        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testAreItemsTheSame() {
        assertTrue(simpleComparator.areItemsTheSame(item0, item0))
    }

    @Test
    fun testAreContentsTheSame() {
        assertTrue(simpleComparator.areContentsTheSame(item0, item0))
    }
}