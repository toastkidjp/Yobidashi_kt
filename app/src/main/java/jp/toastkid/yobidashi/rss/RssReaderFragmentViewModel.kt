/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.rss

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author toastkidjp
 */
class RssReaderFragmentViewModel : ViewModel() {

    private val _itemClick = MutableLiveData<String>()

    val itemClick: LiveData<String> = _itemClick

    fun itemClick(url: String) {
        _itemClick.postValue(url)
    }

}