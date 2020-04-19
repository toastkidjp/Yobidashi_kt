/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author toastkidjp
 */
class BrowserFragmentViewModel : ViewModel() {

    private val _loadWithNewTab = MutableLiveData<Pair<Uri, String>>()

    val loadWithNewTab: LiveData<Pair<Uri, String>> = _loadWithNewTab

    fun loadWithNewTab(next: Pair<Uri, String>) {
        _loadWithNewTab.postValue(next)
    }

}