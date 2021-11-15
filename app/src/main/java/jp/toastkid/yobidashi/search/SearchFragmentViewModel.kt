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
import jp.toastkid.search.value.SearchEvent

class SearchFragmentViewModel : ViewModel() {

    private val _search = MutableLiveData<Event<SearchEvent>>()

    val search: LiveData<Event<SearchEvent>> = _search

    fun search(query: String) {
        _search.postValue(Event(SearchEvent(query)))
    }

    fun searchWithCategory(query: String, category: String) {
        _search.postValue(Event(SearchEvent(query, category)))
    }

    fun searchOnBackground(query: String) {
        _search.postValue(Event(SearchEvent(query, background = true)))
    }

    private val _putQuery = MutableLiveData<Event<String>>()

    val putQuery: LiveData<Event<String>> = _putQuery

    fun putQuery(query: String) {
        _putQuery.postValue(Event(query))
    }
}