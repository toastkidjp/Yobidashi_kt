/*
 * Copyright (c) 2020 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.toastkid.lib.lifecycle.Event

class SearchFragmentViewModel : ViewModel() {

    private val _search = MutableLiveData<Event<Pair<String, Boolean>>>()

    val search: LiveData<Event<Pair<String, Boolean>>> = _search

    fun search(query: String) {
        _search.postValue(Event(query to false))
    }

    fun searchOnBackground(query: String) {
        _search.postValue(Event(query to true))
    }

    private val _putQuery = MutableLiveData<Event<String>>()

    val putQuery: LiveData<Event<String>> = _putQuery

    fun putQuery(query: String) {
        _putQuery.postValue(Event(query))
    }
}