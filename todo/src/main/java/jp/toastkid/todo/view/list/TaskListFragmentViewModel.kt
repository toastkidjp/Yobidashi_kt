/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.view.list

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.toastkid.lib.lifecycle.Event
import jp.toastkid.todo.model.TodoTask

/**
 * @author toastkidjp
 */
class TaskListFragmentViewModel : ViewModel() {

    private val _showMenu = MutableLiveData<Event<Pair<View, TodoTask>>>()

    val showMenu: LiveData<Event<Pair<View, TodoTask>>> = _showMenu

    fun showMenu(view: View, task: TodoTask) {
        _showMenu.postValue(Event(view to task))
    }

}