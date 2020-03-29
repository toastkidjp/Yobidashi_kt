/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.main.content

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * TODO Rename MainActivityViewModel and add snackbar
 * @author toastkidjp
 */
class ContentViewModel : ViewModel() {

    private val _fragmentClass = MutableLiveData<Class<out Fragment>>()

    val fragmentClass: LiveData<Class<out Fragment>> = _fragmentClass

    fun nextFragment(fragmentClass: Class<out Fragment>) {
        _fragmentClass.postValue(fragmentClass)
    }

    private val _fragment = MutableLiveData<Fragment>()

    val fragment: LiveData<Fragment> = _fragment

    fun nextFragment(fragment: Fragment) {
        _fragment.postValue(fragment)
    }

}