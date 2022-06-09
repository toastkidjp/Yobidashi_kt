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
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.BottomAppBar
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
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
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import jp.toastkid.about.view.AboutThisAppUi
import jp.toastkid.article_viewer.article.detail.view.ArticleContentUi
import jp.toastkid.article_viewer.article.list.view.ArticleListUi
import jp.toastkid.barcode.view.BarcodeReaderUi
import jp.toastkid.editor.view.EditorTabUi
import jp.toastkid.image.view.ImageListUi
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.SnackbarEvent
import jp.toastkid.lib.TabListViewModel
import jp.toastkid.lib.intent.OpenDocumentIntentFactory
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.WindowOptionColorApplier
import jp.toastkid.lib.viewmodel.PageSearcherViewModel
import jp.toastkid.loan.view.LoanCalculatorUi
import jp.toastkid.media.music.popup.permission.ReadAudioPermissionRequestContract
import jp.toastkid.media.music.view.MusicListUi
import jp.toastkid.number.NumberPlaceUi
import jp.toastkid.pdf.view.PdfViewerUi
import jp.toastkid.rss.view.RssReaderListUi
import jp.toastkid.search.SearchQueryExtractor
import jp.toastkid.todo.view.board.TaskBoardUi
import jp.toastkid.todo.view.list.TaskListUi
import jp.toastkid.ui.menu.view.OptionMenuItem
import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.archive.view.ArchiveListUi
import jp.toastkid.yobidashi.browser.bookmark.view.BookmarkListUi
import jp.toastkid.yobidashi.browser.floating.view.FloatingPreviewUi
import jp.toastkid.yobidashi.browser.history.view.ViewHistoryListUi
import jp.toastkid.yobidashi.browser.view.WebTabUi
import jp.toastkid.yobidashi.browser.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.calendar.view.CalendarUi
import jp.toastkid.yobidashi.main.RecentAppColoringUseCase
import jp.toastkid.yobidashi.main.StartUp
import jp.toastkid.yobidashi.main.usecase.WebSearchResultTabOpenerUseCase
import jp.toastkid.yobidashi.menu.Menu
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchListUi
import jp.toastkid.yobidashi.search.history.SearchHistoryListUi
import jp.toastkid.yobidashi.search.view.SearchInputUi
import jp.toastkid.yobidashi.settings.view.screen.SettingTopUi
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
import kotlin.math.roundToInt

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
        WindowOptionColorApplier()(activity.window, colorPair)

        RecentAppColoringUseCase(
            activity::getString,
            { activity.resources },
            activity::setTaskDescription,
            Build.VERSION.SDK_INT
        ).invoke(preferenceApplier.color)

        contentViewModel.setScreenFilterColor(preferenceApplier.useColorFilter())
        contentViewModel.setBackgroundImagePath(preferenceApplier.backgroundImagePath)
    })

    val activityResultLauncher: ActivityResultLauncher<Intent>? =
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

    val headerViewModel = ViewModelProvider(activity).get(AppBarViewModel::class.java)

    val scaffoldState = rememberScaffoldState()
    val rememberSnackbarHostState = remember { snackbarHostState }

    val menuFabPosition = preferenceApplier.menuFabPosition()
    val offsetX = remember { mutableStateOf(menuFabPosition?.first ?: 0f) }
    val offsetY = remember { mutableStateOf(menuFabPosition?.second ?: 0f) }
    val openFindInPageState = remember { mutableStateOf(false) }

    val backgroundColor = Color(preferenceApplier.color)
    val tint = Color(preferenceApplier.fontColor)

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

    val mediaPermissionRequestLauncher =
        rememberLauncherForActivityResult(ReadAudioPermissionRequestContract()) {
            it.second?.invoke(it.first)
        }

    val pageSearcherViewModel = viewModel(PageSearcherViewModel::class.java, activity)
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    pageSearcherViewModel.close.observe(activity, {
        it?.getContentIfNotHandled() ?: return@observe
        keyboardController?.hide()
        focusManager.clearFocus(true)
    })
    val pageSearcherInput = remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    contentViewModel?.switchTabList?.observe(activity, Observer {
        it?.getContentIfNotHandled() ?: return@Observer
        contentViewModel?.setCurrentTabId(tabs.currentTabId())
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
                    BottomAppBar(
                        backgroundColor = backgroundColor,
                        elevation = 4.dp,
                        modifier = Modifier
                            .height(72.dp)
                            .offset {
                                IntOffset(
                                    x = 0,
                                    y = -1 * (contentViewModel?.bottomBarOffsetHeightPx?.value?.roundToInt()
                                        ?: 0)
                                )
                            }
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (openFindInPageState.value) {
                                FindInPage(
                                    openFindInPageState,
                                    tint,
                                    pageSearcherInput
                                )
                            } else {
                                headerViewModel.appBarContent.value()
                            }
                        }

                        OverflowMenu(
                            tint,
                            contentViewModel,
                            { navigate(navigationHostController, "setting/top") },
                            { activity.finish() }
                        )
                    }
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
                                        Text(it.actionLabel ?: "", modifier = Modifier
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
                AnimatedNavHost(
                    navController = navigationHostController,
                    startDestination = "empty",
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    contentViewModel?.clearOptionMenus()

                    composable("empty")  {

                    }
                    tabComposable("tab/web/current") {
                        val currentTab = tabs.currentTab() as? WebTab ?: return@tabComposable
                        WebTabUi(currentTab.latest.url().toUri(), currentTab.id())
                    }
                    tabComposable("tab/pdf/current") {
                        val currentTab = tabs.currentTab() as? PdfTab ?: return@tabComposable
                        PdfViewerUi(currentTab.getUrl().toUri())
                    }
                    tabComposable("tab/article/list") {
                        ArticleListUi()
                    }
                    tabComposable("tab/article/content/{title}") {
                        val title = it?.getString("title") ?: return@tabComposable
                        ArticleContentUi(title)
                    }
                    tabComposable("tab/editor/current") {
                        val currentTab = tabs.currentTab() as? EditorTab ?: return@tabComposable
                        EditorTabUi(currentTab.path)
                    }
                    composable("web/bookmark/list") {
                        BookmarkListUi()
                    }
                    composable("web/history/list") {
                        ViewHistoryListUi()
                    }
                    composable("web/archive/list") {
                        ArchiveListUi()
                    }
                    composable("tool/barcode_reader") {
                        BarcodeReaderUi()
                    }
                    composable("tool/image/list") {
                        ImageListUi()
                    }
                    composable("tool/rss/list") {
                        RssReaderListUi()
                    }
                    composable("tool/number/place") {
                        NumberPlaceUi()
                    }
                    composable("tool/task/list") {
                        TaskListUi()
                    }
                    composable("tool/task/board") {
                        TaskBoardUi()
                    }
                    composable("tool/loan") {
                        LoanCalculatorUi()
                    }
                    composable("tab/calendar") {
                        CalendarUi()
                    }
                    composable("setting/top") {
                        SettingTopUi()
                    }
                    composable("search/top") {
                        SearchInputUi()
                    }
                    composable("search/with/?query={query}&title={title}&url={url}") {
                        val query = it.arguments?.getString("query")
                        val title = Uri.decode(it.arguments?.getString("title"))
                        val url = Uri.decode(it.arguments?.getString("url"))
                        SearchInputUi(query, title, url)
                    }
                    composable("search/history/list") {
                        SearchHistoryListUi()
                    }
                    composable("search/favorite/list") {
                        FavoriteSearchListUi()
                    }
                    composable("about") {
                        AboutThisAppUi(BuildConfig.VERSION_NAME)
                    }
                }

                BackHandler(true) {
                    if (openMenu.value) {
                        openMenu.value = false
                        return@BackHandler
                    }
                    if (bottomSheetState.isVisible) {
                        coroutineScope.launch {
                            contentViewModel?.hideBottomSheet()
                        }
                        return@BackHandler
                    }
                    if (openFindInPageState.value) {
                        openFindInPageState.value = false
                        return@BackHandler
                    }
                    val route =
                        navigationHostController?.currentBackStackEntry?.destination?.route
                    navigationHostController?.popBackStack()
                    if (route == "setting/top") {
                        contentViewModel?.refresh()
                    }
                    if (route?.startsWith("tab/") == true) {
                        tabs.closeTab(tabs.index())

                        if (tabs.isEmpty()) {
                            contentViewModel?.switchTabList()
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
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val menuCount = Menu.values().size
                        val tooBigCount = menuCount * 10
                        LazyRow(
                            state = rememberLazyListState(tooBigCount / 2),
                            modifier = Modifier
                                .wrapContentHeight()
                                .fillMaxWidth()
                        ) {
                            items(tooBigCount) { longIndex ->
                                val menu = Menu.values().get(longIndex % menuCount)
                                Surface(
                                    color = backgroundColor,
                                    elevation = 4.dp,
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .clickable {
                                            onClickMainMenuItem(
                                                menu,
                                                contentViewModel,
                                                navigationHostController,
                                                mediaPermissionRequestLauncher,
                                                coroutineScope,
                                                openFindInPageState
                                            )
                                            openMenu.value = false
                                        }
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .size(76.dp)
                                            .padding(4.dp)
                                    ) {
                                        Icon(
                                            painterResource(id = menu.iconId),
                                            contentDescription = stringResource(id = menu.titleId),
                                            tint = tint,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Text(
                                            stringResource(id = menu.titleId),
                                            color = tint,
                                            fontSize = 12.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp)
                                        )
                                    }
                                }
                                BackHandler(openMenu.value) {
                                    openMenu.value = false
                                }
                            }
                        }
                    }
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

@Composable
private fun OverflowMenu(
    tint: Color,
    contentViewModel: ContentViewModel,
    openSetting: () -> Unit,
    finishApp: () -> Unit
) {
    val openOptionMenu = remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .width(32.dp)
        .clickable { openOptionMenu.value = true }) {
        Icon(
            painterResource(id = R.drawable.ic_option_menu),
            contentDescription = stringResource(id = R.string.title_option_menu),
            tint = tint
        )

        val commonOptionMenuItems = listOf(
            OptionMenu(
                titleId = R.string.title_tab_list,
                action = { contentViewModel?.switchTabList() }),
            OptionMenu(
                titleId = R.string.title_settings,
                action = openSetting),
            OptionMenu(titleId = R.string.exit, action = finishApp)
        )
        val menus = contentViewModel?.optionMenus
        val optionMenuItems =
            menus?.union(commonOptionMenuItems)?.distinct()

        DropdownMenu(
            expanded = openOptionMenu.value,
            onDismissRequest = { openOptionMenu.value = false }) {
            optionMenuItems?.forEach {
                DropdownMenuItem(onClick = {
                    openOptionMenu.value = false
                    it.action()
                }) {
                    OptionMenuItem(it)
                }
            }
        }
    }
}

@Composable
private fun FindInPage(
    openFindInPageState: MutableState<Boolean>,
    tint: Color,
    pageSearcherInput: MutableState<String>
) {
    val activity = LocalContext.current as? ViewModelStoreOwner ?: return
    val pageSearcherViewModel = viewModel(PageSearcherViewModel::class.java, activity)
    val closeAction = {
        pageSearcherViewModel.clearInput()
        pageSearcherViewModel.hide()
        openFindInPageState.value = false
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painterResource(id = R.drawable.ic_close),
            contentDescription = stringResource(id = R.string.content_description_close_find_area),
            tint = tint,
            modifier = Modifier
                .clickable(onClick = closeAction)
                .padding(start = 16.dp)
        )

        val focusRequester = remember { FocusRequester() }

        TextField(
            value = pageSearcherInput.value,
            onValueChange = { text ->
                pageSearcherInput.value = text
                pageSearcherViewModel.find(text)
            },
            label = {
                Text(
                    stringResource(id = R.string.hint_find_in_page),
                    color = MaterialTheme.colors.onPrimary
                )
            },
            singleLine = true,
            textStyle = TextStyle(
                color = MaterialTheme.colors.onPrimary,
                textAlign = TextAlign.Start,
            ),
            trailingIcon = {
                Icon(
                    painterResource(R.drawable.ic_clear_form),
                    contentDescription = "clear text",
                    tint = MaterialTheme.colors.onPrimary,
                    modifier = Modifier
                        .clickable {
                            pageSearcherInput.value = ""
                            pageSearcherViewModel.clearInput()
                        }
                )
            },
            maxLines = 1,
            keyboardActions = KeyboardActions {
                pageSearcherViewModel.findDown(pageSearcherInput.value)
            },
            keyboardOptions = KeyboardOptions(
                autoCorrect = true,
                imeAction = ImeAction.Search
            ),
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
                .background(Color.Transparent)
                .focusRequester(focusRequester)
        )
        Icon(
            painterResource(id = R.drawable.ic_up),
            contentDescription = stringResource(id = R.string.content_description_find_upward),
            tint = tint,
            modifier = Modifier
                .clickable {
                    pageSearcherViewModel.findUp(
                        pageSearcherInput.value
                    )
                }
                .padding(8.dp)
        )
        Icon(
            painterResource(id = R.drawable.ic_down),
            contentDescription = stringResource(id = R.string.content_description_find_downward),
            tint = tint,
            modifier = Modifier
                .clickable {
                    pageSearcherViewModel.findDown(
                        pageSearcherInput.value
                    )
                }
                .padding(8.dp)
        )

        BackHandler(openFindInPageState.value) {
            closeAction()
        }

        LaunchedEffect(key1 = "find_in_page_first_launch", block = {
            if (openFindInPageState.value) {
                focusRequester.requestFocus()
            }
        })
    }
}

private fun onClickMainMenuItem(
    menu: Menu,
    contentViewModel: ContentViewModel,
    navigationHostController: NavHostController,
    mediaPermissionRequestLauncher: ManagedActivityResultLauncher<((Boolean) -> Unit)?, Pair<Boolean, ((Boolean) -> Unit)?>>,
    coroutineScope: CoroutineScope,
    openFindInPageState: MutableState<Boolean>
) {
    when (menu) {
        Menu.TOP -> {
            contentViewModel.toTop()
        }
        Menu.BOTTOM -> {
            contentViewModel?.toBottom()
        }
        Menu.SHARE -> {
            contentViewModel?.share()
        }
        Menu.CODE_READER -> {
            navigate(
                navigationHostController,
                "tool/barcode_reader"
            )
        }
        Menu.LOAN_CALCULATOR -> {
            navigate(
                navigationHostController,
                "tool/loan"
            )
        }
        Menu.RSS_READER -> {
            navigate(
                navigationHostController,
                "tool/rss/list"
            )
        }
        Menu.NUMBER_PLACE -> {
            navigate(
                navigationHostController,
                "tool/number/place"
            )
        }
        Menu.AUDIO -> {
            mediaPermissionRequestLauncher.launch {
                if (it.not()) {
                    contentViewModel?.snackShort(R.string.message_requires_permission_storage)
                    return@launch
                }

                contentViewModel?.setBottomSheetContent { MusicListUi() }
                coroutineScope?.launch {
                    contentViewModel?.switchBottomSheet()
                }
            }
        }
        Menu.BOOKMARK -> {
            navigate(
                navigationHostController,
                "web/bookmark/list"
            )
        }
        Menu.VIEW_HISTORY -> {
            navigate(
                navigationHostController,
                "web/history/list"
            )
        }
        Menu.IMAGE_VIEWER -> {
            navigate(
                navigationHostController,
                "tool/image/list"
            )
        }
        Menu.CALENDAR -> {
            contentViewModel?.openCalendar()
        }
        Menu.WEB_SEARCH -> {
            contentViewModel?.webSearch()
        }
        Menu.ABOUT_THIS_APP -> {
            navigate(navigationHostController, "about")
        }
        Menu.TODO_TASKS_BOARD -> {
            navigate(
                navigationHostController,
                "tool/task/board"
            )
        }
        Menu.TODO_TASKS -> {
            navigate(
                navigationHostController,
                "tool/task/list"
            )
        }
        Menu.VIEW_ARCHIVE -> {
            navigate(
                navigationHostController,
                "web/archive/list"
            )
        }
        Menu.FIND_IN_PAGE -> {
            openFindInPageState.value = true
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.tabComposable(route: String, content: @Composable (Bundle?) -> Unit) {
    composable(
        route,
        enterTransition = {
            slideInVertically(initialOffsetY = { it })
        },
        exitTransition = {
            slideOutVertically(targetOffsetY = { it })
        }
    ) {
        content(it.arguments)
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

    contentViewModel?.newArticle?.observe(activity, Observer {
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
