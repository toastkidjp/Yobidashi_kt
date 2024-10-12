/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import jp.toastkid.article_viewer.article.data.ArticleRepositoryFactory
import jp.toastkid.article_viewer.calendar.DateSelectedActionUseCase
import jp.toastkid.display.effect.SnowRendererView
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.input.Inputs
import jp.toastkid.lib.intent.OpenDocumentIntentFactory
import jp.toastkid.lib.model.tab.StartUp
import jp.toastkid.lib.network.DownloadAction
import jp.toastkid.lib.network.NetworkChecker
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.viewmodel.event.content.NavigationEvent
import jp.toastkid.lib.viewmodel.event.content.RefreshContentEvent
import jp.toastkid.lib.viewmodel.event.content.ReplaceToCurrentTabContentEvent
import jp.toastkid.lib.viewmodel.event.content.SnackbarEvent
import jp.toastkid.lib.viewmodel.event.finder.CloseFinderEvent
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
import jp.toastkid.media.music.permission.MusicPlayerPermissions
import jp.toastkid.media.music.view.MusicListUi
import jp.toastkid.search.SearchCategory
import jp.toastkid.search.SearchQueryExtractor
import jp.toastkid.search.UrlFactory
import jp.toastkid.web.floating.view.FloatingPreviewUi
import jp.toastkid.web.permission.DownloadPermissionRequestContract
import jp.toastkid.web.webview.GlobalWebViewPool
import jp.toastkid.web.webview.factory.WebViewClientFactory
import jp.toastkid.web.webview.factory.WebViewFactory
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.main.RecentAppColoringUseCase
import jp.toastkid.yobidashi.main.usecase.ClippingUrlOpener
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
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Content() {
    val activity = LocalContext.current as? ComponentActivity ?: return

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

    val navigationHostController = rememberNavController()

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

    val openMenu = remember { mutableStateOf(false) }

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
                contentViewModel.bottomBarOffsetHeightPx.value = newOffset.coerceIn(-contentViewModel.bottomBarHeightPx(), 0f)

                val newValue = fabOffsetHeightPx.value + (delta / 2)
                fabOffsetHeightPx.value = when {
                    0f > newValue -> 0f
                    newValue > contentViewModel.bottomBarHeightPx() -> contentViewModel.bottomBarHeightPx()
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
                    .snackShort(jp.toastkid.lib.R.string.message_requires_permission_storage)
                return@rememberLauncherForActivityResult
            }
            if (downloadUrl.value.isEmpty()) {
                return@rememberLauncherForActivityResult
            }
            if (PreferenceApplier(activity).wifiOnly && NetworkChecker().isUnavailableWiFi(activity)) {
                contentViewModel.snackShort(jp.toastkid.lib.R.string.message_wifi_not_connecting)
                return@rememberLauncherForActivityResult
            }
            DownloadAction(activity).invoke(downloadUrl.value)
            downloadUrl.value = ""
        }

    val localDensity = LocalDensity.current
    LaunchedEffect(localDensity) {
        contentViewModel.setBottomBarHeightPx(with(localDensity) { 72.dp.toPx() })
    }

    LaunchedEffect(key1 = LocalLifecycleOwner.current, block = {
        val webViewClientFactory = WebViewClientFactory.forBackground(
            activity,
            contentViewModel,
            PreferenceApplier(activity)
        )
        val webViewFactory = WebViewFactory()
        contentViewModel.event.collect {
            when (it) {
                is CloseFinderEvent -> {
                    keyboardController?.hide()
                    focusManager.clearFocus(true)
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
                    activityResultLauncher.launch(OpenDocumentIntentFactory()("application/pdf"))
                }
                is OpenEditorEvent -> {
                    tabs.openNewEditorTab()
                    replaceToCurrentTab(tabs, navigationHostController)
                }
                is SnackbarEvent -> {
                    val message = it.message ?: it.messageId?.let(activity::getString) ?: return@collect
                    contentViewModel.showSnackbar(message, it)
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
                        val message =
                            activity.getString(R.string.message_tab_open_background, title)
                        contentViewModel.showSnackbar(
                            message,
                            SnackbarEvent(
                                message,
                                actionLabel = activity.getString(jp.toastkid.lib.R.string.open)
                            ) {
                                tabs.replace(tab)
                                replaceToCurrentTab(tabs, navigationHostController)
                            }
                        )
                        return@collect
                    }

                    replaceToCurrentTab(tabs, navigationHostController)
                }
                is OpenDateArticleEvent -> {
                    DateSelectedActionUseCase(
                        ArticleRepositoryFactory().invoke(activity),
                        contentViewModel
                    ).invoke(it.year, it.month, it.date, it.background)
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
                    val preferenceApplier = PreferenceApplier(activity)
                    val colorPair = preferenceApplier.colorPair()

                    RecentAppColoringUseCase(
                        activity::getString,
                        { activity.resources },
                        activity::setTaskDescription,
                        Build.VERSION.SDK_INT
                    ).invoke(preferenceApplier.color)

                    contentViewModel.setColorPair(colorPair)
                    contentViewModel.setColorFilterColor(
                        Color(preferenceApplier.filterColor(Color.Transparent.toArgb()))
                    )

                    contentViewModel.setScreenFilterColor(preferenceApplier.useColorFilter())
                    contentViewModel.setBackgroundImagePath(preferenceApplier.backgroundImagePath)
                }
                is OpenNewTabEvent -> {
                    openNewTab(PreferenceApplier(activity), tabs, navigationHostController)
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

                        val webView = webViewFactory.make(activity)
                        webView.webViewClient = webViewClientFactory.invoke()
                        webView.loadUrl(urlString)
                        GlobalWebViewPool.put(newTab.id(), webView)

                        contentViewModel.snackWithAction(
                            activity.getString(R.string.message_tab_open_background, urlString),
                            activity.getString(jp.toastkid.lib.R.string.open)
                        ) {
                            tabs.setIndexByTab(newTab)
                            contentViewModel.replaceToCurrentTab()
                        }
                        return@collect
                    }
                    tabs.openNewWebTab(urlString)
                    replaceToCurrentTab(tabs, navigationHostController)
                }
                is PreviewEvent -> {
                    val uri = if (Urls.isValidUrl(it.text)) it.text.toUri() else
                        UrlFactory().invoke(
                            PreferenceApplier(activity).getDefaultSearchEngine()
                                ?: SearchCategory.getDefaultCategoryName(),
                            it.text
                        )
                    contentViewModel?.setBottomSheetContent {
                        FloatingPreviewUi(uri)
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

                    val webView = webViewFactory.make(activity)
                    webView.webViewClient = webViewClientFactory.invoke()
                    val transport = message.obj as? WebView.WebViewTransport
                    transport?.webView = webView
                    message.sendToTarget()
                    GlobalWebViewPool.put(newTab.id(), webView)

                    replaceToCurrentTab(tabs, navigationHostController)
                }
                is WebSearchEvent -> {
                    WebSearchResultTabOpenerUseCase(
                        PreferenceApplier(activity),
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
                contentViewModel?.snackShort(jp.toastkid.lib.R.string.message_requires_permission_storage)
                return@rememberLauncherForActivityResult
            }

            contentViewModel.switchMusicListUi()
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

        if (contentViewModel.showTabList()) {
            TabListUi(tabs)
        }

        if (contentViewModel.showMusicListUi()) {
            MusicListUi()
        }

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                AppBar()
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = contentViewModel.snackbarHostState(),
                    snackbar = {
                        MainSnackbar(it) { contentViewModel.dismissSnackbar() }
                    })
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { openMenu.value = openMenu.value.not() },
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(48.dp)
                        .scale(contentViewModel.fabScale.value)
                        .offset { contentViewModel.makeFabOffset() }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragEnd = {
                                    PreferenceApplier(activity)
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
                        stringResource(id = jp.toastkid.todo.R.string.menu),
                        tint = MaterialTheme.colorScheme.primary
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

                MainBackHandler(
                    {
                        navigationHostController.currentBackStackEntry?.destination?.route
                    },
                    navigationHostController::popBackStack,
                    tabs::closeCurrentTab,
                    tabs::currentTabIsWebTab,
                    tabs::isEmpty
                )
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
                        mediaPermissionRequestLauncher.launch(MusicPlayerPermissions().invoke())
                    }
                ) { openMenu.value = false }
            }

            if (contentViewModel.useScreenFilter.value) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind { drawRect(contentViewModel.colorFilterColor()) }
                )
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(activity) {
        val lifecycle = lifecycleOwner.lifecycle
        val lifecycleObserver = LifecycleEventObserver { source, event ->
            tabs.onLifecycleEvent(event)
        }
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
        launchSingleTop = true
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
