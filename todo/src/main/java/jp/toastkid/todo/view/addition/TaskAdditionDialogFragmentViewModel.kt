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

/**
 * @author toastkidjp
 */
class TaskAdditionDialogFragmentViewModel : ViewModel() {

    private val _refresh = MutableLiveData<Event<Unit>>()

    val refresh: LiveData<Event<Unit>> = _refresh

    fun refresh() {
        _refresh.postValue(Event(Unit))
    }

}