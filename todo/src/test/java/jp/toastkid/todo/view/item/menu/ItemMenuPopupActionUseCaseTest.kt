/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.todo.view.item.menu

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.todo.model.TodoTask
import org.junit.After
import org.junit.Before
import org.junit.Test

class ItemMenuPopupActionUseCaseTest {

    @InjectMockKs
    private lateinit var itemMenuPopupActionUseCase: ItemMenuPopupActionUseCase

    @MockK
    private lateinit var modifyAction: (TodoTask) -> Unit

    @MockK
    private lateinit var deleteAction: (TodoTask) -> Unit

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { modifyAction.invoke(any()) }.answers { Unit }
        every { deleteAction.invoke(any()) }.answers { Unit }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testModify() {
        itemMenuPopupActionUseCase.modify(mockk())

        verify(exactly = 1) { modifyAction.invoke(any()) }
        verify(exactly = 0) { deleteAction.invoke(any()) }
    }

    @Test
    fun delete() {
        itemMenuPopupActionUseCase.delete(mockk())

        verify(exactly = 0) { modifyAction.invoke(any()) }
        verify(exactly = 1) { deleteAction.invoke(any()) }
    }

}