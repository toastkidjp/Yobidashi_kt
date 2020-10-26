/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.view.addition

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.toastkid.lib.lifecycle.Event
import jp.toastkid.todo.model.TodoTask

/**
 * @author toastkidjp
 */
class TaskAdditionDialogFragmentViewModel : ViewModel() {

    private val _refresh = MutableLiveData<Event<TodoTask>>()

    val refresh: LiveData<Event<TodoTask>> = _refresh

    fun refresh(task: TodoTask) {
        _refresh.postValue(Event(task))
    }

}