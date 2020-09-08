/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.view.addition

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

/**
 * @author toastkidjp
 */
class TaskAdditionDialogFragmentUseCase(
        private val viewLifecycleOwner: Fragment,
        private val refresh: () -> Unit
) {

    operator fun invoke() {
        val taskAdditionDialogFragment = TaskAdditionDialogFragment()
        taskAdditionDialogFragment.setTargetFragment(viewLifecycleOwner, 1)
        ViewModelProvider(viewLifecycleOwner).get(TaskAdditionDialogFragmentViewModel::class.java)
                .refresh
                .observe(viewLifecycleOwner, Observer {
                    it?.getContentIfNotHandled() ?: return@Observer
                    refresh()
                })
        taskAdditionDialogFragment.show(viewLifecycleOwner.parentFragmentManager, "")
    }
}