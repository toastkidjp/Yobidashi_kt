/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author toastkidjp
 */
class HeaderViewModel : ViewModel() {

    val title = MutableLiveData<String?>()

    val url = MutableLiveData<String?>()

    val progress = MutableLiveData<Int>()

    val stopProgress = MutableLiveData<Boolean>()

}