/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author toastkidjp
 */
class ExcludingSettingFragmentViewModel : ViewModel() {

    private val _dismiss = MutableLiveData<Unit>()

    val dismiss: LiveData<Unit> = _dismiss

    fun dismiss() {
        _dismiss.postValue(Unit)
    }
}