/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib

import androidx.annotation.StringRes
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.toastkid.lib.lifecycle.Event
import jp.toastkid.lib.model.OptionMenu

/**
 * @author toastkidjp
 */
class ContentViewModel : ViewModel() {

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

    private val _toTop = MutableLiveData<Event<Unit>>()

    val toTop: LiveData<Event<Unit>> = _toTop

    fun toTop() {
        _toTop.postValue(Event(Unit))
    }

    private val _toBottom = MutableLiveData<Event<Unit>>()

    val toBottom: LiveData<Event<Unit>> = _toBottom

    fun toBottom() {
        _toBottom.postValue(Event(Unit))
    }

    private val _share = MutableLiveData<Event<Unit>>()

    val share: LiveData<Event<Unit>> = _share

    fun share() {
        _share.value = Event(Unit)
    }

    private val _webSearch = MutableLiveData<Event<Unit>>()

    val webSearch: LiveData<Event<Unit>> = _webSearch

    fun webSearch() {
        _webSearch.postValue(Event(Unit))
    }

    private val _openPdf = MutableLiveData<Event<Unit>>()

    val openPdf: LiveData<Event<Unit>> = _openPdf

    fun openPdf() {
        _openPdf.postValue(Event(Unit))
    }

    private val _openEditorTab = MutableLiveData<Event<Unit>>()

    val openEditorTab: LiveData<Event<Unit>> = _openEditorTab

    fun openEditorTab() {
        _openEditorTab.postValue(Event(Unit))
    }

    private val _bottomSheetContent = mutableStateOf<@Composable () -> Unit>({})

    val bottomSheetContent: State<@Composable () -> Unit> = _bottomSheetContent

    fun setBottomSheetContent(content: @Composable () -> Unit) {
        _bottomSheetContent.value = content
    }

    private val _currentTabId = mutableStateOf("")

    val currentTabId: State<String> = _currentTabId

    fun setCurrentTabId(tabId: String) {
        _currentTabId.value = tabId
    }

    @OptIn(ExperimentalMaterialApi::class)
    val modalBottomSheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden)

    @OptIn(ExperimentalMaterialApi::class)
    suspend fun switchBottomSheet() {
        if (modalBottomSheetState.isVisible) {
            modalBottomSheetState.hide()
        } else {
            modalBottomSheetState.show()
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    suspend fun hideBottomSheet() {
        modalBottomSheetState.hide()
    }

    private val _nextRoute = MutableLiveData<Event<String>>()

    val nextRoute: LiveData<Event<String>> = _nextRoute

    fun nextRoute(route: String) {
        _nextRoute.postValue(Event(route))
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

    private val _optionMenus = mutableStateListOf<OptionMenu>()

    val optionMenus: SnapshotStateList<OptionMenu> = _optionMenus

    fun optionMenus(vararg menus: OptionMenu) {
        _optionMenus.clear()
        _optionMenus.addAll(menus.toList())
    }

    fun clearOptionMenus() {
        _optionMenus.clear()
    }

    val bottomBarOffsetHeightPx = mutableStateOf(0f)

    val fabScale = mutableStateOf(1f)

    fun resetComponentVisibility() {
        bottomBarOffsetHeightPx.value = 0f
        fabScale.value = 1f
    }

    private val _replaceToCurrentTab = MutableLiveData<Event<Unit>>()

    val replaceToCurrentTab: LiveData<Event<Unit>> = _replaceToCurrentTab

    fun replaceToCurrentTab() {
        _replaceToCurrentTab.postValue(Event(Unit))
    }

}