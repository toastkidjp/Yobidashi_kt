/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.todo.view.board

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.todo.model.TodoTask
import org.junit.After
import org.junit.Before
import org.junit.Test

class TaskClearUseCaseTest {

    @InjectMockKs
    private lateinit var taskClearUseCase: TaskClearUseCase

    @MockK
    private lateinit var tasks: ArrayList<TodoTask>

    @MockK
    private lateinit var contentViewModel: ContentViewModel

    @MockK
    private lateinit var clearBoard: () -> Unit

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { tasks.toArray() }.answers { arrayOf() }
        every { tasks.clear() }.answers { Unit }
        every { clearBoard.invoke() }.answers { Unit }
        every { contentViewModel.snackWithAction(any(), any(), any()) }.answers { Unit }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        taskClearUseCase.invoke()

        verify(exactly = 1) { tasks.toArray() }
        verify(exactly = 1) { tasks.clear() }
        verify(exactly = 1) { clearBoard.invoke() }
        verify(exactly = 1) { contentViewModel.snackWithAction(any(), any(), any()) }
    }

}