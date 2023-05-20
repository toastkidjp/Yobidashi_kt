/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import jp.toastkid.display.effect.SnowRendererView
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.compat.material3.ModalBottomSheetLayout
import jp.toastkid.lib.input.Inputs
import jp.toastkid.lib.intent.OpenDocumentIntentFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.viewmodel.event.content.NavigationEvent
import jp.toastkid.lib.viewmodel.event.content.RefreshContentEvent
import jp.toastkid.lib.viewmodel.event.content.ReplaceToCurrentTabContentEvent
import jp.toastkid.lib.viewmodel.event.content.SnackbarEvent
import jp.toastkid.lib.viewmodel.event.content.SwitchTabListEvent
import jp.toastkid.lib.viewmodel.event.finder.CloseFinderEvent
import jp.toastkid.lib.viewmodel.event.tab.MoveTabEvent
import jp.toastkid.lib.viewmodel.event.tab.OpenArticleEvent
import jp.toastkid.lib.viewmodel.event.tab.OpenArticleListEvent
import jp.toastkid.lib.viewmodel.event.tab.OpenCalendarEvent
import jp.toastkid.lib.viewmodel.event.tab.OpenEditorEvent
import jp.toastkid.lib.viewmodel.event.tab.OpenNewTabEvent
import jp.toastkid.lib.viewmodel.event.tab.OpenPdfEvent
import jp.toastkid.lib.viewmodel.event.tab.OpenWebSearchEvent
import jp.toastkid.lib.viewmodel.event.tab.SaveEditorTabEvent
import jp.toastkid.lib.viewmodel.event.web.DownloadEvent
import jp.toastkid.lib.viewmodel.event.web.OnLoadCompletedEvent
import jp.toastkid.lib.viewmodel.event.web.OpenNewWindowEvent
import jp.toastkid.lib.viewmodel.event.web.OpenUrlEvent
import jp.toastkid.lib.viewmodel.event.web.PreviewUrlEvent
import jp.toastkid.lib.viewmodel.event.web.WebSearchEvent
import jp.toastkid.media.music.view.MusicListUi
import jp.toastkid.search.SearchQueryExtractor
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.FaviconApplier
import jp.toastkid.yobidashi.browser.block.AdRemover
import jp.toastkid.yobidashi.browser.floating.view.FloatingPreviewUi
import jp.toastkid.yobidashi.browser.permission.DownloadPermissionRequestContract
import jp.toastkid.yobidashi.browser.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.browser.webview.WebViewFactoryUseCase
import jp.toastkid.yobidashi.browser.webview.factory.WebViewClientFactory
import jp.toastkid.yobidashi.libs.clip.ClippingUrlOpener
import jp.toastkid.yobidashi.libs.network.DownloadAction
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

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
internal fun Content() {
    val snackbarHostState = SnackbarHostState()
    val activity = LocalContext.current as? ComponentActivity ?: return

    val preferenceApplier = PreferenceApplier(activity)

    val contentViewModel = viewModel(ContentViewModel::class.java, activity)

    val tabs = remember {
        TabAdapter(
            { activity },
            {
                contentViewModel.switchTabList()
                contentViewModel.openNewTab()
            }
        )
    }

    val navigationHostController = rememberAnimatedNavController()
    navigationHostController.enableOnBackPressed(false)

    val lifecycleOwner = LocalLifecycleOwner.current

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
            contentViewModel.replaceToCurrentTab()
        }

    val requestPermissionForOpenPdfTab =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) {
                return@rememberLauncherForActivityResult
            }

            activityResultLauncher.launch(OpenDocumentIntentFactory()("application/pdf"))
        }

    val openMenu = remember { mutableStateOf(false) }

    val rememberSnackbarHostState = remember { snackbarHostState }

    val backgroundColor = MaterialTheme.colorScheme.primary
    val tint = MaterialTheme.colorScheme.onPrimary

    val bottomBarHeightPx = with(LocalDensity.current) { 72.dp.toPx() }
    contentViewModel.setBottomBarHeightPx(bottomBarHeightPx)

    val coroutineScope = rememberCoroutineScope()

    val fabOffsetHeightPx = remember { mutableStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.Fling) {
                    return Offset.Zero
                }
                val delta = available.y
                val newOffset = contentViewModel.bottomBarOffsetHeightPx.value + delta
                contentViewModel.bottomBarOffsetHeightPx.value = newOffset.coerceIn(-bottomBarHeightPx, 0f)

                val newValue = fabOffsetHeightPx.value + (delta / 2)
                fabOffsetHeightPx.value = when {
                    0f > newValue -> 0f
                    newValue > bottomBarHeightPx -> bottomBarHeightPx
                    else -> newValue
                }

                if (delta < -20f) {
                    contentViewModel.hideFab(coroutineScope)
                } else if (delta > 20f) {
                    contentViewModel.showFab(coroutineScope)
                }
                return Offset.Zero
            }
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val downloadUrl = remember { mutableStateOf("") }
    val downloadPermissionRequestLauncher =
        rememberLauncherForActivityResult(DownloadPermissionRequestContract()) {
            if (it.not()) {
                contentViewModel
                    .snackShort(R.string.message_requires_permission_storage)
                return@rememberLauncherForActivityResult
            }
            if (downloadUrl.value.isEmpty()) {
                return@rememberLauncherForActivityResult
            }
            DownloadAction(activity).invoke(downloadUrl.value)
            downloadUrl.value = ""
        }

    val webViewFactory = remember {
        WebViewFactoryUseCase(
            webViewClientFactory = WebViewClientFactory(
                contentViewModel,
                AdRemover.make(activity.assets),
                FaviconApplier(activity),
                preferenceApplier,
                browserViewModel = ViewModelProvider(activity).get(BrowserViewModel::class.java),
                currentView = { GlobalWebViewPool.getLatest() }
            )
        )
    }

    LaunchedEffect(key1 = lifecycleOwner, block = {
        contentViewModel.event.collect {
            when (it) {
                is CloseFinderEvent -> {
                    keyboardController?.hide()
                    focusManager.clearFocus(true)
                }
                is SwitchTabListEvent -> {
                    contentViewModel?.setBottomSheetContent { TabListUi(tabs) }
                    coroutineScope?.launch {
                        contentViewModel?.switchBottomSheet()
                    }
                }
                is MoveTabEvent -> {
                    if (it.movingStep == -1) {
                        tabs.movePreviousTab()
                    } else {
                        tabs.moveNextTab()
                    }
                    replaceToCurrentTab(tabs, navigationHostController)
                }
                is OpenPdfEvent -> {
                    requestPermissionForOpenPdfTab.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                is OpenEditorEvent -> {
                    tabs.openNewEditorTab()
                    replaceToCurrentTab(tabs, navigationHostController)
                }
                is SnackbarEvent -> {
                    showSnackbar(activity, snackbarHostState, it)
                }
                is OpenWebSearchEvent -> {
                    when (navigationHostController.currentDestination?.route) {
                        "tab/web/current" -> {
                            val currentTabWebView = GlobalWebViewPool.getLatest() ?: return@collect
                            val currentTitle = Uri.encode(currentTabWebView.title)
                            val currentUrl = Uri.encode(currentTabWebView.url)
                            val query = Uri.encode(
                                SearchQueryExtractor().invoke(currentTabWebView.url)
                                    ?.replace("\n", "") ?: ""
                            )
                            navigate(
                                navigationHostController,
                                "search/with/?query=$query&title=$currentTitle&url=$currentUrl"
                            )
                        }
                        else ->
                            navigate(navigationHostController, "search/top")
                    }
                }
                is OpenArticleEvent -> {
                    val title = it.title
                    val onBackground = it.onBackground

                    val tab = tabs.openNewArticleTab(title, onBackground)

                    if (onBackground) {
                        showSnackbar(
                            activity,
                            snackbarHostState, SnackbarEvent(
                                activity.getString(R.string.message_tab_open_background, title),
                                actionLabel = activity.getString(R.string.open)
                            ) {
                                tabs.replace(tab)
                                replaceToCurrentTab(tabs, navigationHostController)
                            }
                        )
                        return@collect
                    }

                    replaceToCurrentTab(tabs, navigationHostController)
                }
                is OpenArticleListEvent -> {
                    tabs.openArticleList()
                    replaceToCurrentTab(tabs, navigationHostController)
                }
                is OpenCalendarEvent -> {
                    tabs.openCalendar()
                    replaceToCurrentTab(tabs, navigationHostController)
                }
                is NavigationEvent -> {
                    navigate(navigationHostController, it.route)
                }
                is ReplaceToCurrentTabContentEvent -> {
                    replaceToCurrentTab(tabs, navigationHostController)
                }
                is RefreshContentEvent -> {
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
                }
                is OpenNewTabEvent -> {
                    openNewTab(preferenceApplier, tabs, navigationHostController)
                }
                is SaveEditorTabEvent -> {
                    val currentTab = tabs.currentTab() as? EditorTab ?: return@collect
                    currentTab.setFileInformation(it.file)
                    tabs.saveTabList()
                }
                is OpenUrlEvent -> {
                    val urlString = it.uri.toString()
                    if (it.onBackground) {
                        val newTab = tabs.openBackgroundTab(it.title ?: urlString, urlString)

                        val webView = webViewFactory.invoke(activity)
                        webView.loadUrl(urlString)
                        GlobalWebViewPool.put(newTab.id(), webView)

                        contentViewModel.snackWithAction(
                            activity.getString(R.string.message_tab_open_background, urlString),
                            activity.getString(R.string.open)
                        ) {
                            tabs.setIndexByTab(newTab)
                            contentViewModel.replaceToCurrentTab()
                        }
                        return@collect
                    }
                    tabs.openNewWebTab(urlString)
                    replaceToCurrentTab(tabs, navigationHostController)
                }
                is PreviewUrlEvent -> {
                    contentViewModel?.setBottomSheetContent {
                        FloatingPreviewUi(it.uri)
                    }
                    coroutineScope?.launch {
                        contentViewModel?.switchBottomSheet()
                    }
                }
                is OnLoadCompletedEvent -> {
                    if (it.loadInformation.expired()) {
                        return@collect
                    }

                    tabs.updateWebTab(it.loadInformation.tabId to History(it.loadInformation.title, it.loadInformation.url))
                    if (tabs.currentTabId() == it.loadInformation.tabId) {
                        val currentWebView =
                            GlobalWebViewPool.get(tabs.currentTabId()) ?: return@collect
                        tabs.saveNewThumbnail(currentWebView)
                    }
                }
                is OpenNewWindowEvent -> {
                    val message = it.resultMessage ?: return@collect
                    val newTab = tabs.openNewWindowWebTab(message)

                    val webView = webViewFactory.invoke(activity)
                    val transport = message.obj as? WebView.WebViewTransport
                    transport?.webView = webView
                    message.sendToTarget()
                    GlobalWebViewPool.put(newTab.id(), webView)

                    replaceToCurrentTab(tabs, navigationHostController)
                }
                is WebSearchEvent -> {
                    WebSearchResultTabOpenerUseCase(
                        preferenceApplier,
                        {
                            tabs.openNewWebTab(it.toString())
                            contentViewModel.replaceToCurrentTab()
                        }
                    ).invoke(it.query)
                }
                is DownloadEvent -> {
                    downloadUrl.value = it.url
                    downloadPermissionRequestLauncher.launch(it.url)
                }
            }
        }
    })

    val localView = LocalView.current

    val windowInfo = LocalWindowInfo.current
    LaunchedEffect(windowInfo.isWindowFocused) {
        ClippingUrlOpener()(activity) {
            Inputs().hideKeyboard(localView)
            contentViewModel.open(it)
        }
    }

    val mediaPermissionRequestLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it.values.any { it.not() }) {
                contentViewModel?.snackShort(R.string.message_requires_permission_storage)
                return@rememberLauncherForActivityResult
            }

            contentViewModel?.setBottomSheetContent { MusicListUi() }
            coroutineScope?.launch {
                contentViewModel?.switchBottomSheet()
            }
        }

    val bottomSheetState = contentViewModel.modalBottomSheetState ?: return

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

        ModalBottomSheetLayout(
            sheetState = bottomSheetState,
            sheetContent = {
                Box(modifier = Modifier.defaultMinSize(1.dp, 1.dp)) {
                    if (bottomSheetState.isVisible) {
                        Inputs().hideKeyboard(localView)

                        contentViewModel.bottomSheetContent.value.invoke()
                    }
                }

                BackHandler(bottomSheetState.isVisible) {
                    coroutineScope.launch {
                        contentViewModel.hideBottomSheet()
                    }
                }
            },
            sheetElevation = 4.dp,
            sheetBackgroundColor = if (bottomSheetState.isVisible) MaterialTheme.colorScheme.surface else Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    AppBar()
                },
                snackbarHost = {
                    SnackbarHost(
                        hostState = rememberSnackbarHostState,
                        snackbar = {
                            MainSnackbar(it) { rememberSnackbarHostState.currentSnackbarData?.dismiss() }
                        })
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { openMenu.value = openMenu.value.not() },
                        containerColor = tint,
                        modifier = Modifier
                            .size(48.dp)
                            .scale(contentViewModel.fabScale.value)
                            .offset { contentViewModel.makeFabOffset() }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragEnd = {
                                        preferenceApplier
                                            .setNewMenuFabPosition(
                                                contentViewModel.menuFabOffsetX.value,
                                                contentViewModel.menuFabOffsetY.value
                                            )
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        contentViewModel.menuFabOffsetX.value += dragAmount.x
                                        contentViewModel.menuFabOffsetY.value += dragAmount.y
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
            ) { _ ->
                Box {
                    NavigationalContent(navigationHostController, tabs)

                    if (contentViewModel.showSnowEffect()) {
                        AndroidView(factory = { SnowRendererView(activity) })
                    }
                }

                LaunchedEffect(key1 = "first_launch", block = {
                    if (tabs.isEmpty()) {
                        contentViewModel.openNewTab()
                        return@LaunchedEffect
                    }

                    if (navigationHostController.currentDestination?.route == "empty") {
                        replaceToCurrentTab(tabs, navigationHostController)
                    }
                })

                if (openMenu.value) {
                    MainMenu(
                        {
                            Inputs().hideKeyboard(localView)
                            navigate(navigationHostController, it)
                        },
                        {
                            val permissions =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    arrayOf(
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    )
                                } else {
                                    arrayOf(
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                    )
                                }
                            mediaPermissionRequestLauncher.launch(permissions)
                        }
                    ) { openMenu.value = false }
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

    MainBackHandler(
        {
            navigationHostController.currentBackStackEntry?.destination?.route
        },
        {
            navigationHostController.popBackStack()
        },
        {
            tabs.closeTab(tabs.index())
        },
        {
            tabs.currentTab() is WebTab
        }
    ) {
        tabs.isEmpty()
    }

    val lifecycle = lifecycleOwner.lifecycle
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

private fun showSnackbar(
    context: Context,
    snackbarHostState: SnackbarHostState,
    snackbarEvent: SnackbarEvent
) {
    val message = snackbarEvent.message ?: snackbarEvent.messageId?.let(context::getString) ?: return
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
