/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.SnackbarEvent
import jp.toastkid.lib.TabListViewModel
import jp.toastkid.lib.intent.OpenDocumentIntentFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.viewmodel.PageSearcherViewModel
import jp.toastkid.search.SearchQueryExtractor
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.floating.view.FloatingPreviewUi
import jp.toastkid.yobidashi.browser.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.main.RecentAppColoringUseCase
import jp.toastkid.yobidashi.main.StartUp
import jp.toastkid.yobidashi.main.usecase.WebSearchResultTabOpenerUseCase
import jp.toastkid.yobidashi.tab.History
import jp.toastkid.yobidashi.tab.TabAdapter
import jp.toastkid.yobidashi.tab.model.ArticleListTab
import jp.toastkid.yobidashi.tab.model.ArticleTab
import jp.toastkid.yobidashi.tab.model.CalendarTab
import jp.toastkid.yobidashi.tab.model.EditorTab
import jp.toastkid.yobidashi.tab.model.PdfTab
import jp.toastkid.yobidashi.tab.model.WebTab
import jp.toastkid.yobidashi.tab.tab_list.view.TabListUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
internal fun Content() {
    val snackbarHostState = SnackbarHostState()
    val activity = LocalContext.current as? ComponentActivity ?: return

    val preferenceApplier = PreferenceApplier(activity)

    val contentViewModel = viewModel(ContentViewModel::class.java, activity)
    contentViewModel.setScreenFilterColor(preferenceApplier.useColorFilter())
    contentViewModel.setBackgroundImagePath(preferenceApplier.backgroundImagePath)

    val tabListViewModel = viewModel(TabListViewModel::class.java, activity)

    val tabs = remember {
        TabAdapter(
            { activity },
            {
                contentViewModel.switchTabList()
                tabListViewModel.openNewTab()
            }
        )
    }

    val navigationHostController = rememberAnimatedNavController()
    navigationHostController.enableOnBackPressed(false)

    tabListViewModel
        .openNewTab
        .observe(activity, {
            it.getContentIfNotHandled() ?: return@observe
            openNewTab(preferenceApplier, tabs, navigationHostController)
        })

    tabListViewModel
        .saveEditorTab
        .observe(
            activity,
            Observer {
                val currentTab = tabs.currentTab() as? EditorTab ?: return@Observer
                currentTab.setFileInformation(it)
                tabs.saveTabList()
            }
        )

    initializeContentViewModel(activity, tabs, navigationHostController, snackbarHostState)

    contentViewModel.replaceToCurrentTab.observe(activity, {
        it.getContentIfNotHandled() ?: return@observe
        replaceToCurrentTab(tabs, navigationHostController)
    })
    contentViewModel.refresh.observe(activity, {
        val colorPair = preferenceApplier.colorPair()

        RecentAppColoringUseCase(
            activity::getString,
            { activity.resources },
            activity::setTaskDescription,
            Build.VERSION.SDK_INT
        ).invoke(preferenceApplier.color)

        contentViewModel.setColorPair(colorPair)

        contentViewModel.setScreenFilterColor(preferenceApplier.useColorFilter())
        contentViewModel.setBackgroundImagePath(preferenceApplier.backgroundImagePath)
    })

    val activityResultLauncher: ActivityResultLauncher<Intent> =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) {
                return@rememberLauncherForActivityResult
            }

            val data = it.data ?: return@rememberLauncherForActivityResult
            val uri = data.data ?: return@rememberLauncherForActivityResult
            activity.contentResolver?.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            tabs.openNewPdfTab(uri)
            contentViewModel?.replaceToCurrentTab()
            contentViewModel?.switchTabList()
        }

    val requestPermissionForOpenPdfTab =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) {
                return@rememberLauncherForActivityResult
            }

            activityResultLauncher?.launch(OpenDocumentIntentFactory()("application/pdf"))
        }

    val openMenu = remember { mutableStateOf(false) }

    val scaffoldState = rememberScaffoldState()
    val rememberSnackbarHostState = remember { snackbarHostState }

    val menuFabPosition = preferenceApplier.menuFabPosition()
    val offsetX = remember { mutableStateOf(menuFabPosition?.first ?: 0f) }
    val offsetY = remember { mutableStateOf(menuFabPosition?.second ?: 0f) }
    val openFindInPageState = remember { mutableStateOf(false) }

    val backgroundColor = MaterialTheme.colors.primary
    val tint = MaterialTheme.colors.onPrimary

    val bottomBarHeight = 72.dp
    val bottomBarHeightPx = with(LocalDensity.current) {
        bottomBarHeight.roundToPx().toFloat()
    }

    val fabOffsetHeightPx = remember { mutableStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = (contentViewModel?.bottomBarOffsetHeightPx?.value ?: 0f) + delta
                contentViewModel?.bottomBarOffsetHeightPx?.value = newOffset.coerceIn(-bottomBarHeightPx, 0f)

                val newValue = fabOffsetHeightPx.value + (delta / 2)
                fabOffsetHeightPx.value = when {
                    0f > newValue -> 0f
                    newValue > bottomBarHeight.value -> bottomBarHeight.value
                    else -> newValue
                }

                contentViewModel?.fabScale?.value = fabOffsetHeightPx.value / bottomBarHeight.value
                return Offset.Zero
            }
        }
    }

    val pageSearcherViewModel = viewModel(PageSearcherViewModel::class.java, activity)
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    pageSearcherViewModel.close.observe(activity, {
        it?.getContentIfNotHandled() ?: return@observe
        keyboardController?.hide()
        focusManager.clearFocus(true)
    })

    val coroutineScope = rememberCoroutineScope()

    contentViewModel?.switchTabList?.observe(activity, Observer {
        it?.getContentIfNotHandled() ?: return@Observer
        contentViewModel?.setBottomSheetContent { TabListUi(tabs) }
        coroutineScope?.launch {
            contentViewModel?.switchBottomSheet()
        }
    })
    contentViewModel?.openPdf?.observe(activity, {
        it?.getContentIfNotHandled() ?: return@observe
        requestPermissionForOpenPdfTab.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    })
    contentViewModel?.openEditorTab?.observe(activity, {
        it?.getContentIfNotHandled() ?: return@observe
        tabs.openNewEditorTab()
        replaceToCurrentTab(tabs, navigationHostController)
    })

    val browserViewModel = viewModel(BrowserViewModel::class.java, activity)
    browserViewModel?.open?.observe(activity, Observer {
        val uri = it?.getContentIfNotHandled() ?: return@Observer
        tabs.openNewWebTab(uri.toString())
        replaceToCurrentTab(tabs, navigationHostController)
    })
    browserViewModel?.preview?.observe(activity, Observer {
        val uri = it?.getContentIfNotHandled() ?: return@Observer
        contentViewModel?.setBottomSheetContent {
            FloatingPreviewUi(uri)
        }
        coroutineScope?.launch {
            contentViewModel?.switchBottomSheet()
        }
    })
    browserViewModel?.openBackground?.observe(activity, Observer {
        val urlString = it?.getContentIfNotHandled()?.toString() ?: return@Observer
        val callback = tabs.openBackgroundTab(urlString, urlString)
        contentViewModel.snackWithAction(
            activity.getString(R.string.message_tab_open_background, urlString),
            activity.getString(R.string.open)
        ) {
            callback()
            contentViewModel.replaceToCurrentTab()
        }
    })
    browserViewModel?.openBackgroundWithTitle?.observe(activity, Observer {
        val pair = it?.getContentIfNotHandled() ?: return@Observer
        val callback = tabs.openBackgroundTab(pair.first, pair.second.toString())
        contentViewModel.snackWithAction(
            activity.getString(R.string.message_tab_open_background, pair.first),
            activity.getString(R.string.open)
        ) {
            callback()
            contentViewModel.replaceToCurrentTab()
        }
    })
    browserViewModel?.openNewWindow?.observe(activity, Observer {
        val message = it?.getContentIfNotHandled() ?: return@Observer
        tabs.openNewWindowWebTab(message)
        browserViewModel?.switchWebViewToCurrent(tabs.currentTabId())
    })
    LaunchedEffect(browserViewModel) {
        browserViewModel
            .onPageFinished
            .observe(activity, {
                if (it.expired()) {
                    return@observe
                }

                tabs.updateWebTab(it.tabId to History(it.title, it.url))
                if (tabs.currentTabId() == it.tabId) {
                    val currentWebView =
                        GlobalWebViewPool.get(tabs.currentTabId()) ?: return@observe
                    tabs.saveNewThumbnail(currentWebView)
                }
            })
        browserViewModel
            .search
            .observe(activity, { event ->
                val query = event?.getContentIfNotHandled() ?: return@observe
                WebSearchResultTabOpenerUseCase(
                    preferenceApplier,
                    {
                        tabs.openNewWebTab(it.toString())
                        contentViewModel.replaceToCurrentTab()
                    }
                ).invoke(query)
            })
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AsyncImage(
            contentViewModel.backgroundImagePath.value,
            contentDescription = stringResource(R.string.content_description_background),
            alignment = Alignment.Center,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        val bottomSheetState = contentViewModel?.modalBottomSheetState ?: return

        ModalBottomSheetLayout(
            sheetState = bottomSheetState,
            sheetContent = {
                Box(modifier = Modifier.defaultMinSize(1.dp, 1.dp)) {
                    if (bottomSheetState.isVisible) {
                        keyboardController?.hide()

                        contentViewModel?.bottomSheetContent?.value?.invoke()
                    }
                }
            },
            sheetElevation = 4.dp,
            sheetBackgroundColor = if (bottomSheetState.isVisible) MaterialTheme.colors.surface else Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Scaffold(
                scaffoldState = scaffoldState,
                backgroundColor = Color.Transparent,
                bottomBar = {
                    AppBar(
                        openFindInPageState,
                        { navigate(navigationHostController, "setting/top") }
                    )
                },
                snackbarHost = {
                    SnackbarHost(
                        hostState = rememberSnackbarHostState,
                        snackbar = {
                            Snackbar(
                                backgroundColor = backgroundColor,
                                contentColor = tint,
                                elevation = 4.dp
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        it.message,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (it.actionLabel != null) {
                                        Text(
                                            it.actionLabel ?: "",
                                            modifier = Modifier
                                                .clickable {
                                                    it.performAction()
                                                }
                                                .wrapContentWidth())
                                    }
                                }
                            }
                        })
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { openMenu.value = openMenu.value.not() },
                        backgroundColor = tint,
                        modifier = Modifier
                            .scale(contentViewModel?.fabScale?.value ?: 1f)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragEnd = {
                                        preferenceApplier
                                            .setNewCameraFabPosition(
                                                offsetX.value,
                                                offsetY.value
                                            )
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consumeAllChanges()
                                        offsetX.value += dragAmount.x
                                        offsetY.value += dragAmount.y
                                    }
                                )
                            }
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_menu),
                            stringResource(id = R.string.menu),
                            tint = backgroundColor
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection)
            ) {
                NavigationalContent(navigationHostController, tabs)

                BackHandler(true) {
                    if (openMenu.value) {
                        openMenu.value = false
                        return@BackHandler
                    }
                    if (bottomSheetState.isVisible) {
                        coroutineScope.launch {
                            contentViewModel.hideBottomSheet()
                        }
                        return@BackHandler
                    }
                    if (openFindInPageState.value) {
                        openFindInPageState.value = false
                        return@BackHandler
                    }

                    val route =
                        navigationHostController.currentBackStackEntry?.destination?.route

                    if (route?.startsWith("tab/web") == true) {
                        val latest = GlobalWebViewPool.getLatest()
                        if (latest?.canGoBack() == true) {
                            latest.goBack()
                            return@BackHandler
                        }
                    }

                    navigationHostController.popBackStack()
                    if (route == "setting/top") {
                        contentViewModel.refresh()
                    }

                    if (route?.startsWith("tab/") == true) {
                        tabs.closeTab(tabs.index())

                        if (tabs.isEmpty()) {
                            contentViewModel.switchTabList()
                            openNewTab(preferenceApplier, tabs, navigationHostController)
                            return@BackHandler
                        }
                        replaceToCurrentTab(tabs, navigationHostController)
                        return@BackHandler
                    }

                    if (route == "empty" || route == null) {
                        activity.finish()
                    }
                }

                LaunchedEffect(key1 = "first_launch", block = {
                    if (tabs.isEmpty()) {
                        tabListViewModel.openNewTab()
                        return@LaunchedEffect
                    }

                    if (navigationHostController.currentDestination?.route == "empty") {
                        replaceToCurrentTab(tabs, navigationHostController)
                    }
                })

                if (openMenu.value) {
                    MainMenu(
                        openFindInPageState,
                        { navigate(navigationHostController, it) },
                        { openMenu.value = false }
                    )
                }

                if (contentViewModel.useScreenFilter.value) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(preferenceApplier.filterColor(Color.Transparent.toArgb())))
                    )
                }
            }
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val lifecycleObserver = LifecycleEventObserver { source, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> tabs.setCount()
            Lifecycle.Event.ON_PAUSE -> tabs.saveTabList()
            Lifecycle.Event.ON_DESTROY -> tabs.dispose()
            else -> Unit
        }
    }
    DisposableEffect(activity) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
}

private fun navigate(navigationController: NavHostController?, route: String) {
    if (navigationController?.currentDestination?.route?.startsWith("tab/") == false
        || route.startsWith("tab/")) {
        navigationController?.popBackStack()
    }

    navigationController?.navigate(route) {
        if (route.startsWith("tab/")) {
            anim {
                enter = R.anim.slide_up
            }
        }
        launchSingleTop = true
    }
}

private fun initializeContentViewModel(
    activity: ComponentActivity,
    tabs: TabAdapter,
    navigationHostController: NavHostController,
    snackbarHostState: SnackbarHostState
) {
    val contentViewModel = ViewModelProvider(activity).get(ContentViewModel::class.java)
    contentViewModel?.snackbar?.observe(activity, Observer {
        val snackbarEvent = it.getContentIfNotHandled() ?: return@Observer
        showSnackbar(snackbarHostState, snackbarEvent)
    })
    contentViewModel?.snackbarRes?.observe(activity, Observer {
        val messageId = it?.getContentIfNotHandled() ?: return@Observer
        showSnackbar(snackbarHostState, SnackbarEvent(activity.getString(messageId)))
    })
    contentViewModel?.webSearch?.observe(activity, {
        it?.getContentIfNotHandled() ?: return@observe
        when (navigationHostController?.currentDestination?.route) {
            "tab/web/current" -> {
                val currentTabWebView = GlobalWebViewPool.getLatest() ?: return@observe
                val currentTitle = Uri.encode(currentTabWebView.title)
                val currentUrl = Uri.encode(currentTabWebView.url)
                val query = SearchQueryExtractor().invoke(currentTabWebView.url) ?: ""
                navigate(
                    navigationHostController,
                    "search/with/?query=$query&title=$currentTitle&url=$currentUrl"
                )
            }
            else ->
                navigate(navigationHostController, "search/top")
        }
    })

    contentViewModel.newArticle.observe(activity, Observer {
        val titleAndOnBackground = it?.getContentIfNotHandled() ?: return@Observer

        val title = titleAndOnBackground.first
        val onBackground = titleAndOnBackground.second

        val tab = tabs.openNewArticleTab(title, onBackground)

        if (onBackground) {
            showSnackbar(
                snackbarHostState, SnackbarEvent(
                    activity.getString(R.string.message_tab_open_background, title),
                    activity.getString(R.string.open)
                ) {
                    tabs.replace(tab)
                    replaceToCurrentTab(tabs, navigationHostController)
                }
            )
            return@Observer
        }

        replaceToCurrentTab(tabs, navigationHostController)
    })
    contentViewModel?.openArticleList?.observe(activity, {
        it?.getContentIfNotHandled() ?: return@observe
        tabs.openArticleList()
        replaceToCurrentTab(tabs, navigationHostController)
    })
    contentViewModel?.openCalendar?.observe(activity, {
        it?.getContentIfNotHandled() ?: return@observe
        tabs.openCalendar()
        replaceToCurrentTab(tabs, navigationHostController)
    })
    contentViewModel?.nextRoute?.observe(activity, {
        val route = it?.getContentIfNotHandled() ?: return@observe
        navigate(navigationHostController, route)
    })
}

private fun showSnackbar(
    snackbarHostState: SnackbarHostState,
    snackbarEvent: SnackbarEvent
) {
    if (snackbarEvent.actionLabel == null) {
        CoroutineScope(Dispatchers.Main).launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(snackbarEvent.message)
        }
        return
    }

    CoroutineScope(Dispatchers.Main).launch {
        snackbarHostState.currentSnackbarData?.dismiss()
        val snackbarResult = snackbarHostState.showSnackbar(
            snackbarEvent.message,
            snackbarEvent.actionLabel ?: "",
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

/**
 * Replace visibilities for current tab.
 */
private fun replaceToCurrentTab(tabs: TabAdapter, navigationHostController: NavHostController) {
    val route = when (val tab = tabs.currentTab()) {
        is WebTab -> {
            "tab/web/current"
        }
        is PdfTab -> {
            "tab/pdf/current"
        }
        is ArticleListTab -> {
            "tab/article/list"
        }
        is ArticleTab -> {
            "tab/article/content/${tab.title()}"
        }
        is CalendarTab -> {
            "tab/calendar"
        }
        is EditorTab -> {
            "tab/editor/current"
        }
        else -> {
            null
        }
    } ?: return
    navigate(navigationHostController, route)
}

private fun openNewTab(
    preferenceApplier: PreferenceApplier,
    tabs: TabAdapter,
    navigationHostController: NavHostController
) {
    when (StartUp.findByName(preferenceApplier.startUp)) {
        StartUp.SEARCH -> {
            navigate(navigationHostController, "search/top")
        }
        StartUp.BROWSER -> {
            tabs.openNewWebTab()
            replaceToCurrentTab(tabs, navigationHostController)
        }
        StartUp.BOOKMARK -> {
            navigate(navigationHostController, "web/bookmark/list")
        }
    }
}
