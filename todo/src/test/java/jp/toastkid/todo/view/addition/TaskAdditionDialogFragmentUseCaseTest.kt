/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.todo.view.addition

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import jp.toastkid.lib.lifecycle.Event
import jp.toastkid.todo.model.TodoTask
import org.junit.After
import org.junit.Before
import org.junit.Test

class TaskAdditionDialogFragmentUseCaseTest {

    @InjectMockKs
    private lateinit var taskAdditionDialogFragmentUseCase: TaskAdditionDialogFragmentUseCase

    @MockK
    private lateinit var viewLifecycleOwner: Fragment

    @MockK
    private lateinit var viewModel: TaskAdditionDialogFragmentViewModel

    @MockK
    private lateinit var taskConsumer: (TodoTask) -> Unit

    @MockK
    private lateinit var taskAdditionDialogFragment: TaskAdditionDialogFragment

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { viewLifecycleOwner.getParentFragmentManager() }.returns(mockk())
        every { viewLifecycleOwner.getViewModelStore() }.returns(mockk())
        every { viewLifecycleOwner.getDefaultViewModelProviderFactory() }.returns(mockk())
        every { taskConsumer.invoke(any()) }.answers { Unit }

        val refresh = mockk<LiveData<Event<TodoTask>>>()
        every { refresh.observe(any(), any()) }.answers { Unit }
        every { viewModel.refresh }.returns(refresh)

        mockkObject(TaskAdditionDialogFragment)
        every { TaskAdditionDialogFragment.make(any()) }.returns(taskAdditionDialogFragment)
        every { taskAdditionDialogFragment.setTargetFragment(any(), any()) }.answers { Unit }
        every { taskAdditionDialogFragment.show(any<FragmentManager>(), any<String>()) }.answers { Unit }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        taskAdditionDialogFragmentUseCase.invoke(mockk())
    }

}