/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.toastkid.lib.lifecycle.Event

/**
 * @author toastkidjp
 */
class ContentViewModel : ViewModel() {

    private val _fragmentClass = MutableLiveData<Event<Class<out Fragment>>>()

    val fragmentClass: LiveData<Event<Class<out Fragment>>> = _fragmentClass

    fun nextFragment(fragmentClass: Class<out Fragment>) {
        _fragmentClass.postValue(Event(fragmentClass))
    }

    private val _fragment = MutableLiveData<Event<Fragment>>()

    val fragment: LiveData<Event<Fragment>> = _fragment

    fun nextFragment(fragment: Fragment) {
        _fragment.postValue(Event(fragment))
    }

    private val _snackbar = MutableLiveData<Event<SnackbarEvent>>()

    val snackbar: LiveData<Event<SnackbarEvent>> = _snackbar

    fun snackShort(message: String) {
        _snackbar.postValue(Event(SnackbarEvent(message)))
    }

    private val _snackbarRes = MutableLiveData<Event<Int>>()

    val snackbarRes: LiveData<Event<Int>> = _snackbarRes

    fun snackShort(@StringRes messageId: Int) {
        _snackbarRes.postValue(Event(messageId))
    }

    fun snackWithAction(message: String, actionLabel: String, action: () -> Unit) {
        _snackbar.postValue(Event(SnackbarEvent(message, actionLabel, action)))
    }

    private val _toTop = MutableLiveData<Unit>()

    val toTop: LiveData<Unit> = _toTop

    fun toTop() {
        _toTop.postValue(Unit)
    }

    private val _toBottom = MutableLiveData<Unit>()

    val toBottom: LiveData<Unit> = _toBottom

    fun toBottom() {
        _toBottom.postValue(Unit)
    }

    private val _share = MutableLiveData<Event<Unit>>()

    val share: LiveData<Event<Unit>> = _share

    fun share() {
        _share.value = Event(Unit)
    }

    private val _webSearch = MutableLiveData<Unit>()

    val webSearch: LiveData<Unit> = _webSearch

    fun webSearch() {
        _webSearch.postValue(Unit)
    }

    private val _openPdf = MutableLiveData<Unit>()

    val openPdf: LiveData<Unit> = _openPdf

    fun openPdf() {
        _openPdf.postValue(Unit)
    }

    private val _openEditorTab = MutableLiveData<Unit>()

    val openEditorTab: LiveData<Unit> = _openEditorTab

    fun openEditorTab() {
        _openEditorTab.postValue(Unit)
    }

    private val _switchPageSearcher = MutableLiveData<Unit>()

    val switchPageSearcher: LiveData<Unit> = _switchPageSearcher

    fun switchPageSearcher() {
        _switchPageSearcher.postValue(Unit)
    }

    private val _switchTabList = MutableLiveData<Event<Unit>>()

    val switchTabList: LiveData<Event<Unit>> = _switchTabList

    fun switchTabList() {
        _switchTabList.postValue(Event(Unit))
    }

    private val _refresh = MutableLiveData<Unit>()

    val refresh: LiveData<Unit> = _refresh

    fun refresh() {
        _refresh.postValue(Unit)
    }

    private val _newArticle = MutableLiveData<Event<Pair<String, Boolean>>>()

    val newArticle: LiveData<Event<Pair<String, Boolean>>> = _newArticle

    fun newArticle(title: String) {
        _newArticle.postValue(Event(title to false))
    }

    fun newArticleOnBackground(title: String) {
        _newArticle.postValue(Event(title to true))
    }

    private val _openArticleList = MutableLiveData<Event<Unit>>()

    val openArticleList: LiveData<Event<Unit>> = _openArticleList

    fun openArticleList() {
        _openArticleList.postValue(Event(Unit))
    }

    private val _openCalendar = MutableLiveData<Event<Unit>>()

    val openCalendar: LiveData<Event<Unit>> = _openCalendar

    fun openCalendar() {
        _openCalendar.postValue(Event(Unit))
    }

}