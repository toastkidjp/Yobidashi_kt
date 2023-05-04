/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.toastkid.lib.compat.material3.ModalBottomSheetState
import jp.toastkid.lib.compat.material3.ModalBottomSheetValue
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.viewmodel.event.Event
import jp.toastkid.lib.viewmodel.event.content.NavigationEvent
import jp.toastkid.lib.viewmodel.event.content.RefreshContentEvent
import jp.toastkid.lib.viewmodel.event.content.ReplaceToCurrentTabContentEvent
import jp.toastkid.lib.viewmodel.event.content.ShareEvent
import jp.toastkid.lib.viewmodel.event.content.SnackbarEvent
import jp.toastkid.lib.viewmodel.event.content.SwitchTabListEvent
import jp.toastkid.lib.viewmodel.event.content.ToBottomEvent
import jp.toastkid.lib.viewmodel.event.content.ToTopEvent
import jp.toastkid.lib.viewmodel.event.tab.MoveTabEvent
import jp.toastkid.lib.viewmodel.event.tab.OpenArticleEvent
import jp.toastkid.lib.viewmodel.event.tab.OpenArticleListEvent
import jp.toastkid.lib.viewmodel.event.tab.OpenCalendarEvent
import jp.toastkid.lib.viewmodel.event.tab.OpenEditorEvent
import jp.toastkid.lib.viewmodel.event.tab.OpenPdfEvent
import jp.toastkid.lib.viewmodel.event.tab.OpenWebSearchEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * @author toastkidjp
 */
class ContentViewModel : ViewModel() {

    private val _event = MutableSharedFlow<Event>()

    val event = _event.asSharedFlow()

    private val colorPair = mutableStateOf(ColorPair(Color.White.toArgb(), Color.Black.toArgb()))

    fun colorPair(): State<ColorPair> {
        return colorPair
    }

    fun setColorPair(colorPair: ColorPair) {
        this.colorPair.value = colorPair
    }

    fun snackShort(message: String) {
        viewModelScope.launch {
            _event.emit(SnackbarEvent(message))
        }
    }

    fun snackShort(@StringRes messageId: Int) {
        viewModelScope.launch {
            _event.emit(SnackbarEvent(messageId = messageId))
        }
    }

    fun snackWithAction(message: String, actionLabel: String, action: () -> Unit) {
        viewModelScope.launch {
            _event.emit(SnackbarEvent(message, actionLabel = actionLabel, action = action))
        }
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

    private val _bottomSheetContent = mutableStateOf<@Composable () -> Unit>({})

    val bottomSheetContent: State<@Composable () -> Unit> = _bottomSheetContent

    fun setBottomSheetContent(content: @Composable () -> Unit) {
        _bottomSheetContent.value = content
    }

    private val _hideBottomSheetAction = mutableStateOf({})

    fun setHideBottomSheetAction(action: () -> Unit) {
        _hideBottomSheetAction.value = action
    }

    val modalBottomSheetState = ModalBottomSheetState(
        ModalBottomSheetValue.Hidden,
        confirmStateChange = {
            if (it == ModalBottomSheetValue.Hidden) {
                _hideBottomSheetAction.value()
            }
            true
        }
    )

    suspend fun switchBottomSheet() {
        if (modalBottomSheetState.isVisible) {
            modalBottomSheetState.hide()
        } else {
            modalBottomSheetState.show()
        }
    }

    suspend fun hideBottomSheet() {
        _hideBottomSheetAction.value()
        modalBottomSheetState.hide()
    }

    fun nextRoute(route: String) {
        viewModelScope.launch {
            _event.emit(NavigationEvent(route))
        }
    }

    fun switchTabList() {
        viewModelScope.launch {
            _event.emit(SwitchTabListEvent())
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

    private var bottomBarHeightPx = 0f

    fun setBottomBarHeightPx(float: Float) {
        if (bottomBarHeightPx == 0f) {
            bottomBarHeightPx = float
        }
    }

    fun showAppBar(coroutineScope: CoroutineScope? = null) {
        bottomBarOffsetHeightPx.value = 0f

        coroutineScope?.let {
            showFab(coroutineScope)
        }
    }

    fun hideAppBar() {
        bottomBarOffsetHeightPx.value = -bottomBarHeightPx
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
        setShowDisplayEffect(preferenceApplier.showDisplayEffect())
        preferenceApplier.menuFabPosition()?.let {
            setMenuFabPosition(it.first, it.second)
        }
        setScreenFilterColor(preferenceApplier.useColorFilter())
        setBackgroundImagePath(preferenceApplier.backgroundImagePath)
    }

}