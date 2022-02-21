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

class SimpleComparatorTest {

    private lateinit var simpleComparator: SimpleComparator

    @RelaxedMockK
    private lateinit var item0: TodoTask

    @org.junit.Before
    fun setUp() {
        simpleComparator = SimpleComparator()

        MockKAnnotations.init(this)
    }

    @org.junit.After
    fun tearDown() {
        unmockkAll()
    }

    @org.junit.Test
    fun areItemsTheSame() {
        simpleComparator.areItemsTheSame(item0, item0)
    }

    @org.junit.Test
    fun areContentsTheSame() {
    }
}