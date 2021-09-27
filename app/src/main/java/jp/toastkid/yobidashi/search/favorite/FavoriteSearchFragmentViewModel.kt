/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.toastkid.lib.lifecycle.Event
import java.io.Serializable

/**
 * @author toastkidjp
 */
class FavoriteSearchFragmentViewModel : ViewModel(), Serializable {

    private val _reload = MutableLiveData<Event<Unit>>()

    val reload: LiveData<Event<Unit>> = _reload

    fun reload() {
        _reload.postValue(Event(Unit))
    }

    private val _clear = MutableLiveData<Event<Unit>>()

    val clear: LiveData<Event<Unit>> = _clear

    fun clear() {
        _clear.postValue(Event(Unit))
    }
}