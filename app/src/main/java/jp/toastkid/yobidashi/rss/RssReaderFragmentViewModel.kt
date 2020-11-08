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
import jp.toastkid.lib.lifecycle.Event

/**
 * @author toastkidjp
 */
class RssReaderFragmentViewModel : ViewModel() {

    private val _itemClick = MutableLiveData<Event<Pair<String, Boolean>>>()

    val itemClick: LiveData<Event<Pair<String, Boolean>>> = _itemClick

    fun itemClick(url: String, onBackground: Boolean = false) {
        _itemClick.postValue(Event(url to onBackground))
    }

}