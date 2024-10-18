/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib

import android.net.Uri
import android.os.Message
import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.toastkid.lib.model.LoadInformation
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.scroll.StateScroller
import jp.toastkid.lib.viewmodel.event.Event
import jp.toastkid.lib.viewmodel.event.content.NavigationEvent
import jp.toastkid.lib.viewmodel.event.content.RefreshContentEvent
import jp.toastkid.lib.viewmodel.event.content.ReplaceToCurrentTabContentEvent
import jp.toastkid.lib.viewmodel.event.content.ShareEvent
import jp.toastkid.lib.viewmodel.event.content.SnackbarEvent
import jp.toastkid.lib.viewmodel.event.content.ToBottomEvent
import jp.toastkid.lib.viewmodel.event.content.ToTopEvent
import jp.toastkid.lib.viewmodel.event.finder.ClearFinderInputEvent
import jp.toastkid.lib.viewmodel.event.finder.CloseFinderEvent
import jp.toastkid.lib.viewmodel.event.finder.FindAllEvent
import jp.toastkid.lib.viewmodel.event.finder.FindInPageEvent
import jp.toastkid.lib.viewmodel.event.tab.MoveTabEvent
import jp.toastkid.lib.viewmodel.event.tab.OpenArticleEvent
import jp.toastkid.lib.viewmodel.event.tab.OpenArticleListEvent
import jp.toastkid.lib.viewmodel.event.tab.OpenCalendarEvent
import jp.toastkid.lib.viewmodel.event.tab.OpenDateArticleEvent
import jp.toastkid.lib.viewmodel.event.tab.OpenEditorEvent
import jp.toastkid.lib.viewmodel.event.tab.OpenNewTabEvent
import jp.toastkid.lib.viewmodel.event.tab.OpenPdfEvent
import jp.toastkid.lib.viewmodel.event.tab.OpenWebSearchEvent
import jp.toastkid.lib.viewmodel.event.tab.SaveEditorTabEvent
import jp.toastkid.lib.viewmodel.event.web.DownloadEvent
import jp.toastkid.lib.viewmodel.event.web.OnLoadCompletedEvent
import jp.toastkid.lib.viewmodel.event.web.OpenNewWindowEvent
import jp.toastkid.lib.viewmodel.event.web.OpenUrlEvent
import jp.toastkid.lib.viewmodel.event.web.PreviewEvent
import jp.toastkid.lib.viewmodel.event.web.WebSearchEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * @author toastkidjp
 */
class ContentViewModel : ViewModel() {

    private val _event = MutableSharedFlow<Event>()

    val event = _event.asSharedFlow()

    suspend fun receiveEvent(scroller: StateScroller) {
        event.collect {
            when (it) {
                is ToTopEvent -> {
                    scroller.toTop()
                }

                is ToBottomEvent -> {
                    scroller.toBottom()
                }
            }
        }
    }

    suspend fun <T> receiveEvent(
        scroller: StateScroller,
        listItemState: SnapshotStateList<T>,
        fullItems: Collection<T>,
        predicate: ((T, String) -> Boolean)
    ) {
        event.collect {
            when (it) {
                is ToTopEvent -> {
                    scroller.toTop()
                }
                is ToBottomEvent -> {
                    scroller.toBottom()
                }
                is FindInPageEvent -> {
                    listItemState.clear()
                    if (it.word.isBlank()) {
                        listItemState.addAll(fullItems)
                        return@collect
                    }

                    listItemState.addAll(
                        fullItems.filter { item -> predicate(item, it.word) }
                    )
                }
                else -> Unit
            }
        }
    }

    private val colorPair = mutableStateOf(ColorPair(Color.White.toArgb(), Color.Black.toArgb()))

    fun colorPair(): ColorPair {
        return colorPair.value
    }

    fun setColorPair(colorPair: ColorPair) {
        this.colorPair.value = colorPair
    }

    fun toTop() {
        viewModelScope.launch {
            _event.emit(ToTopEvent())
        }
    }

    fun toBottom() {
        viewModelScope.launch {
            _event.emit(ToBottomEvent())
        }
    }

    fun share() {
        viewModelScope.launch {
            _event.emit(ShareEvent())
        }
    }

    fun webSearch() {
        viewModelScope.launch {
            _event.emit(OpenWebSearchEvent())
        }
    }

    fun openPdf() {
        viewModelScope.launch {
            _event.emit(OpenPdfEvent())
        }
    }

    fun openEditorTab() {
        viewModelScope.launch {
            _event.emit(OpenEditorEvent())
        }
    }

    private val showTabList = mutableStateOf(false)

    fun showTabList() = showTabList.value

    fun switchTabList() {
        showTabList.value = showTabList.value.not()
    }

    private val showMusicListUi = mutableStateOf(false)

    fun showMusicListUi() = showMusicListUi.value

    fun switchMusicListUi() {
        showMusicListUi.value = showMusicListUi.value.not()
    }

    private val floatingPreviewUrl = mutableStateOf<String?>(null)

    fun showFloatingPreviewUi() = floatingPreviewUrl.value != null

    fun floatingPreviewUri() = floatingPreviewUrl.value

    fun switchFloatingPreviewUi(url: String? = null) {
        floatingPreviewUrl.value = url
    }

    fun nextRoute(route: String) {
        viewModelScope.launch {
            _event.emit(NavigationEvent(route))
        }
    }

    fun previousTab() {
        viewModelScope.launch {
            _event.emit(MoveTabEvent(-1))
        }
    }

    fun nextTab() {
        viewModelScope.launch {
            _event.emit(MoveTabEvent(1))
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _event.emit(RefreshContentEvent())
        }
    }

    fun newArticle(title: String) {
        viewModelScope.launch {
            _event.emit(OpenArticleEvent(title))
        }
    }

    fun newArticleOnBackground(title: String) {
        viewModelScope.launch {
            _event.emit(OpenArticleEvent(title, true))
        }
    }

    fun openArticleList() {
        viewModelScope.launch {
            _event.emit(OpenArticleListEvent())
        }
    }

    fun openCalendar() {
        viewModelScope.launch {
            _event.emit(OpenCalendarEvent())
        }
    }

    private val _optionMenus = mutableListOf<OptionMenu>()

    val optionMenus: List<OptionMenu> = _optionMenus

    fun optionMenus(vararg menus: OptionMenu) {
        _optionMenus.clear()
        _optionMenus.addAll(menus.toList())
    }

    fun clearOptionMenus() {
        _optionMenus.clear()
    }

    val bottomBarOffsetHeightPx = mutableStateOf(0f)

    val fabScale = Animatable(1f)

    fun showFab(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            fabScale.animateTo(1f)
        }
    }

    fun hideFab(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            fabScale.animateTo(0f)
        }
    }

    fun replaceToCurrentTab() {
        viewModelScope.launch {
            _event.emit(ReplaceToCurrentTabContentEvent())
        }
    }

    private val _useScreenFilter = mutableStateOf(false)

    val useScreenFilter: State<Boolean> = _useScreenFilter

    fun setScreenFilterColor(use: Boolean) {
        _useScreenFilter.value = use
    }

    private val _backgroundImagePath = mutableStateOf("")

    val backgroundImagePath: State<String> = _backgroundImagePath

    fun setBackgroundImagePath(path: String) {
        _backgroundImagePath.value = path
    }

    private val _appBarContent = mutableStateOf<@Composable () -> Unit>({})

    val appBarContent: State<@Composable () -> Unit> = _appBarContent

    fun replaceAppBarContent(composable: @Composable () -> Unit) {
        _appBarContent.value = composable
    }

    private val maxBottomBarHeightPx = AtomicReference(0f)

    fun setBottomBarHeightPx(float: Float) {
        maxBottomBarHeightPx.set(float)
    }

    fun bottomBarHeightPx() = maxBottomBarHeightPx.get()

    fun showAppBar(coroutineScope: CoroutineScope? = null) {
        bottomBarOffsetHeightPx.value = 0f

        coroutineScope?.let {
            showFab(coroutineScope)
        }
    }

    fun hideAppBar() {
        bottomBarOffsetHeightPx.value = -maxBottomBarHeightPx.get()
    }

    val openFindInPageState = mutableStateOf(false)

    private val showSnowEffect = mutableStateOf(false)

    fun setShowDisplayEffect(newState: Boolean) {
        showSnowEffect.value = newState
    }

    fun showSnowEffect() = showSnowEffect.value

    val menuFabOffsetX = mutableStateOf(0f)

    val menuFabOffsetY = mutableStateOf(0f)

    fun makeFabOffset() =
        IntOffset(menuFabOffsetX.value.toInt(), menuFabOffsetY.value.toInt())

    private fun setMenuFabPosition(x: Float, y: Float) {
        menuFabOffsetX.value = x
        menuFabOffsetY.value = y
    }

    fun resetMenuFabPosition() {
        menuFabOffsetX.value = 0f
        menuFabOffsetY.value = 0f
    }

    fun initializeWith(preferenceApplier: PreferenceApplier) {
        setColorPair(preferenceApplier.colorPair())
        setColorFilterColor(Color(preferenceApplier.filterColor(Color.Transparent.toArgb())))
        setShowDisplayEffect(preferenceApplier.showDisplayEffect())
        preferenceApplier.menuFabPosition()?.let {
            setMenuFabPosition(it.first, it.second)
        }
        setScreenFilterColor(preferenceApplier.useColorFilter())
        setBackgroundImagePath(preferenceApplier.backgroundImagePath)
    }

    private val menuScrollState = LazyListState(60)

    fun menuScrollState() = menuScrollState

    fun saveEditorTab(nextFile: File) {
        viewModelScope.launch {
            _event.emit(SaveEditorTabEvent(nextFile))
        }
    }

    private val _tabCount = mutableStateOf(0)

    val tabCount: State<Int> = _tabCount

    fun tabCount(count: Int) {
        _tabCount.value = count
    }

    fun openNewTab() {
        viewModelScope.launch {
            _event.emit(OpenNewTabEvent())
        }
    }

    fun find(s: String?) {
        viewModelScope.launch {
            _event.emit(FindAllEvent(s ?: ""))
        }
    }

    fun findDown(s: String?) {
        viewModelScope.launch {
            _event.emit(FindInPageEvent(s ?: ""))
        }
    }

    fun findUp(s: String?) {
        viewModelScope.launch {
            _event.emit(FindInPageEvent(s ?: "", true))
        }
    }

    fun hideFinder() {
        viewModelScope.launch {
            _event.emit(CloseFinderEvent())
        }
    }

    fun clearFinderInput() {
        viewModelScope.launch {
            _event.emit(ClearFinderInputEvent())
        }
    }

    fun closeFindInPage() {
        clearFinderInput()
        hideFinder()
        openFindInPageState.value = false
    }

    fun preview(text: String) {
        viewModelScope.launch {
            _event.emit(PreviewEvent(text))
        }
    }

    fun open(uri: Uri) {
        viewModelScope.launch {
            _event.emit(OpenUrlEvent(uri))
        }
    }

    fun openBackground(uri: Uri) {
        viewModelScope.launch {
            _event.emit(OpenUrlEvent(uri, true))
        }
    }

    fun openBackground(title: String, uri: Uri) {
        viewModelScope.launch {
            _event.emit(OpenUrlEvent(uri, true, title))
        }
    }

    fun openNewWindow(resultMessage: Message?) {
        viewModelScope.launch {
            _event.emit(OpenNewWindowEvent(resultMessage))
        }
    }

    fun download(url: String) {
        viewModelScope.launch {
            _event.emit(DownloadEvent(url))
        }
    }

    fun finished(tabId: String, title: String, url: String) =
        viewModelScope.launch {
            _event.emit(OnLoadCompletedEvent(LoadInformation(tabId, title, url)))
        }

    fun search(query: String) {
        viewModelScope.launch {
            _event.emit(WebSearchEvent(query))
        }
    }

    fun openDateArticle(year: Int, month: Int, date: Int, background: Boolean = false) {
        viewModelScope.launch {
            _event.emit(OpenDateArticleEvent(year, month, date, background))
        }
    }

    private val _snackbarHostState = SnackbarHostState()

    fun snackbarHostState() = _snackbarHostState

    fun dismissSnackbar() {
        _snackbarHostState.currentSnackbarData?.dismiss()
    }

    fun snackShort(message: String) {
        showSnackbar(message, SnackbarEvent(message))
    }

    fun snackShort(@StringRes messageId: Int) {
        viewModelScope.launch {
            _event.emit(SnackbarEvent(messageId = messageId))
        }
    }

    fun snackWithAction(message: String, actionLabel: String, action: () -> Unit) {
        showSnackbar(message, SnackbarEvent(message, actionLabel = actionLabel, action = action))
    }

    fun showSnackbar(
        message: String,
        snackbarEvent: SnackbarEvent
    ) {
        val snackbarHostState = snackbarHostState()

        if (snackbarEvent.actionLabel == null) {
            CoroutineScope(Dispatchers.Main).launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(message)
            }
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val snackbarResult = snackbarHostState.showSnackbar(
                message,
                snackbarEvent.actionLabel ?: "",
                false,
                SnackbarDuration.Long
            )
            when (snackbarResult) {
                SnackbarResult.Dismissed -> Unit
                SnackbarResult.ActionPerformed -> {
                    snackbarEvent.action()
                }
            }
        }
    }

    private val colorFilterColor = mutableStateOf(Color.Transparent)

    fun colorFilterColor() = colorFilterColor.value

    fun setColorFilterColor(color: Color) {
        colorFilterColor.value = color
    }

    private val lastCalendarPage = AtomicInteger(-1)

    fun lastCalendarPage() = lastCalendarPage.get()

    fun setLastCalendarPage(page: Int) {
        lastCalendarPage.set(page)
    }

}