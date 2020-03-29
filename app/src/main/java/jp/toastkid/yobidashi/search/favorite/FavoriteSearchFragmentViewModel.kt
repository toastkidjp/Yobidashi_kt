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

/**
 * @author toastkidjp
 */
class FavoriteSearchFragmentViewModel : ViewModel() {

    private val _reload = MutableLiveData<Unit>()

    val reload: LiveData<Unit> = _reload

    fun reload() {
        _reload.postValue(Unit)
    }

    private val _clear = MutableLiveData<Unit>()

    val clear: LiveData<Unit> = _clear

    fun clear() {
        _clear.postValue(Unit)
    }
}