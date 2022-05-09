/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import jp.toastkid.about.view.AboutThisAppUi
import jp.toastkid.article_viewer.article.detail.view.ArticleContentUi
import jp.toastkid.article_viewer.article.list.view.ArticleListUi
import jp.toastkid.barcode.view.BarcodeReaderUi
import jp.toastkid.image.view.ImageListTopUi
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
import jp.toastkid.lib.viewmodel.WebSearchViewModel
import jp.toastkid.loan.view.LoanCalculatorUi
import jp.toastkid.media.music.popup.permission.ReadAudioPermissionRequestContract
import jp.toastkid.media.music.view.MusicListUi
import jp.toastkid.pdf.view.PdfViewerUi
import jp.toastkid.rss.view.RssReaderListUi
import jp.toastkid.search.SearchQueryExtractor
import jp.toastkid.todo.view.board.TaskBoardUi
import jp.toastkid.todo.view.list.TaskListUi
import jp.toastkid.ui.menu.view.OptionMenuItem
import jp.toastkid.ui.theme.AppTheme
import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.LoadingViewModel
import jp.toastkid.yobidashi.browser.archive.view.ArchiveListUi
import jp.toastkid.yobidashi.browser.bookmark.view.BookmarkListUi
import jp.toastkid.yobidashi.browser.floating.view.FloatingPreviewUi
import jp.toastkid.yobidashi.browser.history.view.ViewHistoryListUi
import jp.toastkid.yobidashi.browser.permission.DownloadPermissionRequestContract
import jp.toastkid.yobidashi.browser.view.WebTabUi
import jp.toastkid.yobidashi.browser.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.calendar.view.CalendarUi
import jp.toastkid.yobidashi.editor.view.EditorTabUi
import jp.toastkid.yobidashi.libs.clip.ClippingUrlOpener
import jp.toastkid.yobidashi.libs.network.DownloadAction
import jp.toastkid.yobidashi.main.usecase.WebSearchResultTabOpenerUseCase
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchListUi
import jp.toastkid.yobidashi.search.history.SearchHistoryListUi
import jp.toastkid.yobidashi.search.view.SearchInputUi
import jp.toastkid.yobidashi.settings.fragment.OverlayColorFilterViewModel
import jp.toastkid.yobidashi.settings.view.screen.SettingTopUi
import jp.toastkid.yobidashi.tab.TabAdapter
import jp.toastkid.yobidashi.tab.model.ArticleListTab
import jp.toastkid.yobidashi.tab.model.ArticleTab
import jp.toastkid.yobidashi.tab.model.CalendarTab
import jp.toastkid.yobidashi.tab.model.EditorTab
import jp.toastkid.yobidashi.tab.model.PdfTab
import jp.toastkid.yobidashi.tab.model.Tab
import jp.toastkid.yobidashi.tab.model.WebTab
import jp.toastkid.yobidashi.tab.tab_list.Callback
import jp.toastkid.yobidashi.tab.tab_list.view.TabListUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

typealias AppMenu = jp.toastkid.yobidashi.menu.Menu

class MainActivity : ComponentActivity(), Callback {

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    private lateinit var tabs: TabAdapter

    private var contentViewModel: ContentViewModel? = null

    private var tabListViewModel: TabListViewModel? = null

    private var browserViewModel: BrowserViewModel? = null

    private var activityResultLauncher: ActivityResultLauncher<Intent>? =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }

            val data = it.data ?: return@registerForActivityResult
            val uri = data.data ?: return@registerForActivityResult
            val takeFlags: Int =
                data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver?.takePersistableUriPermission(uri, takeFlags)

            tabs.openNewPdfTab(uri)
            replaceToCurrentTab(true)
            contentViewModel?.switchTabList()
        }

    private val requestPermissionForOpenPdfTab =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) {
                return@registerForActivityResult
            }

            activityResultLauncher?.launch(OpenDocumentIntentFactory()("application/pdf"))
        }

    private val mediaPermissionRequestLauncher =
        registerForActivityResult(ReadAudioPermissionRequestContract()) {
            it.second?.invoke(it.first)
        }

    private val musicPlayerBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            //TODO musicPlayerUseCase?.invoke(binding.root)
        }
    }

    private val downloadPermissionRequestLauncher =
        registerForActivityResult(DownloadPermissionRequestContract()) {
            if (it.first.not()) {
                contentViewModel?.snackShort(R.string.message_requires_permission_storage)
                return@registerForActivityResult
            }
            val url = it.second ?: return@registerForActivityResult
            DownloadAction(this).invoke(url)
        }

    private var navigationHostController: NavHostController? = null

    private var filterColor: MutableState<Boolean>? = null

    private var backgroundPath: MutableState<String>? = null

    private var coroutineScope: CoroutineScope? = null

    /**
     * Disposables.
     */
    private val disposables: Job by lazy { Job() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceApplier = PreferenceApplier(this)

        ViewModelProvider(this).get(WebSearchViewModel::class.java)
            .search
            .observe(this, { event ->
                val query = event?.getContentIfNotHandled() ?: return@observe
                WebSearchResultTabOpenerUseCase(preferenceApplier, { openNewWebTab(it)})
                    .invoke(query)
            })

        val activityViewModelProvider = ViewModelProvider(this)
        browserViewModel = activityViewModelProvider.get(BrowserViewModel::class.java)
        browserViewModel?.preview?.observe(this, Observer {
            val uri = it?.getContentIfNotHandled() ?: return@Observer
            contentViewModel?.setBottomSheetContent {
                FloatingPreviewUi(uri)
            }
            coroutineScope?.launch {
                contentViewModel?.switchBottomSheet()
            }
        })
        browserViewModel?.open?.observe(this, Observer {
            val uri = it?.getContentIfNotHandled() ?: return@Observer
            openNewWebTab(uri)
        })

        browserViewModel?.openBackground?.observe(this, Observer {
            val urlString = it?.getContentIfNotHandled()?.toString() ?: return@Observer
            openTabOnBackground(urlString, urlString)
        })
        browserViewModel?.openBackgroundWithTitle?.observe(this, Observer {
            val pair = it?.getContentIfNotHandled() ?: return@Observer
            openTabOnBackground(pair.first, pair.second.toString())
        })
        browserViewModel?.openNewWindow?.observe(this, Observer {
            val message = it?.getContentIfNotHandled() ?: return@Observer
            tabs.openNewWindowWebTab(message)
            browserViewModel?.switchWebViewToCurrent(tabs.currentTabId())
        })
        browserViewModel?.download?.observe(this, Observer {
            val url = it?.getContentIfNotHandled() ?: return@Observer
            downloadPermissionRequestLauncher.launch(url)
        })

        CoroutineScope(Dispatchers.Main).launch {
            activityViewModelProvider.get(LoadingViewModel::class.java)
                .onPageFinished
                .collect {
                    if (it.expired()) {
                        return@collect
                    }

                    tabs.updateWebTab(it.tabId to it.history)
                    if (tabs.currentTabId() == it.tabId) {
                        val currentWebView =
                            GlobalWebViewPool.get(tabs.currentTabId()) ?: return@collect
                        tabs.saveNewThumbnail(currentWebView)
                    }
                }
        }

        activityViewModelProvider.get(OverlayColorFilterViewModel::class.java)
            .newColor
            .observe(this, {
                updateColorFilter()
            })

        tabListViewModel = activityViewModelProvider.get(TabListViewModel::class.java)
        tabListViewModel
            ?.saveEditorTab
            ?.observe(
                this,
                Observer {
                    val currentTab = tabs.currentTab() as? EditorTab ?: return@Observer
                    currentTab.setFileInformation(it)
                    tabs.saveTabList()
                }
            )
        tabListViewModel
            ?.openNewTab
            ?.observe(this, { openNewTabFromTabList() })

        /*supportFragmentManager.setFragmentResultListener("clear_tabs", this, { key, result ->
            if (result.getBoolean(key).not()) {
                return@setFragmentResultListener
            }
            onClickClear()
        })*/

        tabs = TabAdapter({ this }, this::onEmptyTabs)

        registerReceiver(musicPlayerBroadcastReceiver, IntentFilter("jp.toastkid.music.action.open"))

        processShortcut(intent)

        setContent {
            AppTheme(isSystemInDarkTheme() || preferenceApplier.useDarkMode()) {
                Content()
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
    @Composable
    private fun Content() {
        val snackbarHostState = SnackbarHostState()
        initializeContentViewModel(snackbarHostState)

        val openMenu = remember { mutableStateOf(false) }

        val headerViewModel = ViewModelProvider(this).get(AppBarViewModel::class.java)

        val scaffoldState = rememberScaffoldState()
        val rememberSnackbarHostState = remember { snackbarHostState }

        val menuFabPosition = preferenceApplier.menuFabPosition()
        val offsetX = remember { mutableStateOf(menuFabPosition?.first ?: 0f) }
        val offsetY = remember { mutableStateOf(menuFabPosition?.second ?: 0f) }
        val openFindInPageState = remember { mutableStateOf(false) }

        val backgroundColor = Color(preferenceApplier.color)
        val tint = Color(preferenceApplier.fontColor)

        val navigationController = rememberNavController()
        this.navigationHostController = navigationController

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

        val pageSearcherViewModel = viewModel(PageSearcherViewModel::class.java)
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        pageSearcherViewModel.close.observe(this, {
            it?.getContentIfNotHandled() ?: return@observe
            keyboardController?.hide()
            focusManager.clearFocus(true)
        })
        val pageSearcherInput = remember { mutableStateOf("") }

        val color = remember {
            mutableStateOf(preferenceApplier.useColorFilter())
        }
        this.filterColor = color

        val backgroundPath = remember {
            mutableStateOf(preferenceApplier.backgroundImagePath)
        }
        this.backgroundPath = backgroundPath

        contentViewModel?.switchTabList?.observe(this@MainActivity, Observer {
            it?.getContentIfNotHandled() ?: return@Observer
            contentViewModel?.setCurrentTabId(tabs.currentTabId())
            contentViewModel?.setBottomSheetContent { TabListUi() }
            coroutineScope?.launch {
                contentViewModel?.switchBottomSheet()
            }
        })

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AsyncImage(
                backgroundPath.value, // TODO add contentViewModel
                contentDescription = stringResource(R.string.content_description_background),
                alignment = Alignment.Center,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            val bottomSheetState = contentViewModel?.modalBottomSheetState ?: return

            val coroutineScope = rememberCoroutineScope()
            this@MainActivity.coroutineScope = coroutineScope

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
                                        y = -1 * (contentViewModel?.bottomBarOffsetHeightPx?.value?.roundToInt() ?: 0)
                                    )
                                }
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (openFindInPageState.value) {
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
                                                    color = Color(preferenceApplier.fontColor)
                                                )
                                            },
                                            singleLine = true,
                                            textStyle = TextStyle(
                                                color = Color(preferenceApplier.fontColor),
                                                textAlign = TextAlign.Start,
                                            ),
                                            trailingIcon = {
                                                Icon(
                                                    painterResource(R.drawable.ic_clear_form),
                                                    contentDescription = "clear text",
                                                    tint = Color(preferenceApplier.fontColor),
                                                    modifier = Modifier
                                                        //.offset(x = 8.dp)
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
                                } else {
                                    headerViewModel.appBarContent.value()
                                }
                            }

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
                                        action = { navigate(navigationController, "setting/top") }),
                                    OptionMenu(titleId = R.string.exit, action = { finish() })
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
                    navigationController.enableOnBackPressed(false)
                    NavHost(
                        navController = navigationController,
                        startDestination = "empty",
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        contentViewModel?.clearOptionMenus()

                        composable("empty") {

                        }
                        composable("tab/web/current") {
                            val currentTab = tabs.currentTab() as? WebTab ?: return@composable
                            WebTabUi(currentTab.latest.url().toUri(), currentTab.id())
                        }
                        composable("tab/pdf/current") {
                            val currentTab = tabs.currentTab() as? PdfTab ?: return@composable
                            PdfViewerUi(currentTab.getUrl().toUri())
                        }
                        composable("tab/article/list") {
                            ArticleListUi()
                        }
                        composable("tab/article/content/{title}") {
                            val title = it.arguments?.getString("title") ?: return@composable
                            ArticleContentUi(title)
                        }
                        composable("tab/editor/current") {
                            val currentTab = tabs.currentTab() as? EditorTab ?: return@composable
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
                            ImageListTopUi()
                        }
                        composable("tool/rss/list") {
                            RssReaderListUi()
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
                        composable("tool/calendar") {
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
                            refresh()
                        }
                        if (route?.startsWith("tab/") == true) {
                            tabs.closeTab(tabs.index())

                            if (tabs.isEmpty()) {
                                onEmptyTabs()
                                return@BackHandler
                            }
                            replaceToCurrentTab(true)
                            return@BackHandler
                        }

                        if (route == "empty" || route == null) {
                            finish()
                        }
                    }

                    LaunchedEffect(key1 = "first_launch", block = {
                        if (tabs.isEmpty()) {
                            openNewTab()
                            return@LaunchedEffect
                        }

                        if (navigationHostController?.currentDestination?.route == "empty") {
                            replaceToCurrentTab(true)
                        }
                    })

                    if (openMenu.value) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val menuCount = AppMenu.values().size
                            val tooBigCount = menuCount * 5
                            LazyRow(
                                state = rememberLazyListState(tooBigCount / 2),
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .fillMaxWidth()
                            ) {
                                items(tooBigCount) { longIndex ->
                                    val menu = AppMenu.values().get(longIndex % menuCount)
                                    Surface(
                                        color = backgroundColor,
                                        elevation = 4.dp,
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .clickable {
                                                when (menu) {
                                                    AppMenu.TOP -> {
                                                        contentViewModel?.toTop()
                                                    }
                                                    AppMenu.BOTTOM -> {
                                                        contentViewModel?.toBottom()
                                                    }
                                                    AppMenu.SHARE -> {
                                                        contentViewModel?.share()
                                                    }
                                                    AppMenu.CODE_READER -> {
                                                        navigate(
                                                            navigationController,
                                                            "tool/barcode_reader"
                                                        )
                                                    }
                                                    AppMenu.LOAN_CALCULATOR -> {
                                                        navigate(
                                                            navigationController,
                                                            "tool/loan"
                                                        )
                                                    }
                                                    AppMenu.RSS_READER -> {
                                                        navigate(
                                                            navigationController,
                                                            "tool/rss/list"
                                                        )
                                                    }
                                                    AppMenu.AUDIO -> {
                                                        showMusicPlayer()
                                                    }
                                                    AppMenu.BOOKMARK -> {
                                                        navigate(
                                                            navigationController,
                                                            "web/bookmark/list"
                                                        )
                                                    }
                                                    AppMenu.VIEW_HISTORY -> {
                                                        navigate(
                                                            navigationController,
                                                            "web/history/list"
                                                        )
                                                    }
                                                    AppMenu.IMAGE_VIEWER -> {
                                                        navigate(
                                                            navigationController,
                                                            "tool/image/list"
                                                        )
                                                    }
                                                    AppMenu.CALENDAR -> {
                                                        contentViewModel?.openCalendar()
                                                    }
                                                    AppMenu.WEB_SEARCH -> {
                                                        contentViewModel?.webSearch()
                                                    }
                                                    AppMenu.ABOUT_THIS_APP -> {
                                                        navigate(navigationController, "about")
                                                    }
                                                    AppMenu.TODO_TASKS_BOARD -> {
                                                        navigate(
                                                            navigationController,
                                                            "tool/task/board"
                                                        )
                                                    }
                                                    AppMenu.TODO_TASKS -> {
                                                        navigate(
                                                            navigationController,
                                                            "tool/task/list"
                                                        )
                                                    }
                                                    AppMenu.VIEW_ARCHIVE -> {
                                                        navigate(
                                                            navigationController,
                                                            "web/archive/list"
                                                        )
                                                    }
                                                    AppMenu.FIND_IN_PAGE -> {
                                                        openFindInPageState.value = true
                                                    }
                                                }
                                                openMenu.value = false
                                            }
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .size(72.dp)
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
                                                modifier = Modifier.padding(8.dp)
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

                    if (color.value) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(preferenceApplier.filterColor(Color.Transparent.toArgb())))
                        )
                    }
                }
            }
        }
    }

    private fun navigate(navigationController: NavHostController?, route: String) {
        contentViewModel?.resetComponentVisibility()

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

    private fun showMusicPlayer() {
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

    private fun initializeContentViewModel(snackbarHostState: SnackbarHostState) {
        contentViewModel = ViewModelProvider(this).get(ContentViewModel::class.java)
        contentViewModel?.snackbar?.observe(this, Observer {
            val snackbarEvent = it.getContentIfNotHandled() ?: return@Observer
            showSnackbar(snackbarHostState, snackbarEvent)
        })
        contentViewModel?.snackbarRes?.observe(this, Observer {
            val messageId = it?.getContentIfNotHandled() ?: return@Observer
            showSnackbar(snackbarHostState, SnackbarEvent(getString(messageId)))
        })
        contentViewModel?.webSearch?.observe(this, {
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
        contentViewModel?.openPdf?.observe(this, {
            it?.getContentIfNotHandled() ?: return@observe
            openPdfTabFromStorage()
        })
        contentViewModel?.openEditorTab?.observe(this, {
            it?.getContentIfNotHandled() ?: return@observe
            openEditorTab()
        })
        contentViewModel?.refresh?.observe(this, {
            refresh()
        })
        contentViewModel?.newArticle?.observe(this, Observer {
            val titleAndOnBackground = it?.getContentIfNotHandled() ?: return@Observer

            val title = titleAndOnBackground.first
            val onBackground = titleAndOnBackground.second

            val tab = tabs.openNewArticleTab(title, onBackground)

            if (onBackground) {
                showSnackbar(
                    snackbarHostState, SnackbarEvent(
                        getString(R.string.message_tab_open_background, title),
                        getString(R.string.open)
                    ) {
                        tabs.replace(tab)
                        replaceToCurrentTab()
                    }
                )
                return@Observer
            }

            replaceToCurrentTab()
        })
        contentViewModel?.openArticleList?.observe(this, {
            it?.getContentIfNotHandled() ?: return@observe
            tabs.openArticleList()
            replaceToCurrentTab()
        })
        contentViewModel?.openCalendar?.observe(this, {
            it?.getContentIfNotHandled() ?: return@observe
            tabs.openCalendar()
            replaceToCurrentTab()
        })
        contentViewModel?.nextRoute?.observe(this, {
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
                snackbarHostState.showSnackbar(snackbarEvent.message)
            }
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
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

    override fun onNewIntent(passedIntent: Intent) {
        super.onNewIntent(passedIntent)
        processShortcut(passedIntent)
    }

    /**
     * Process intent shortcut.
     *
     * @param calledIntent
     */
    private fun processShortcut(calledIntent: Intent) {

    }

    private fun search(category: String?, query: String?) {
        if (category.isNullOrEmpty() || query.isNullOrEmpty()) {
            return
        }

        SearchAction(this, category, query).invoke()
    }

    private fun openNewWebTab(uri: Uri) {
        tabs.openNewWebTab(uri.toString())
        replaceToCurrentTab(true)
    }

    /**
     * Replace visibilities for current tab.
     *
     * @param withAnimation for suppress redundant animation.
     */
    private fun replaceToCurrentTab(withAnimation: Boolean = true) {
        //tabReplacingUseCase.invoke(withAnimation)
        when (val tab = tabs.currentTab()) {
            is WebTab -> {
                if (navigationHostController?.currentDestination?.route == "tab/web/current") {
                    ViewModelProvider(this).get(BrowserViewModel::class.java)
                        .loadWithNewTab(tab.latest.url().toUri() to tab.latest.title())
                    return
                }
                navigate(navigationHostController, "tab/web/current")
            }
            is PdfTab -> {
                navigate(navigationHostController, "tab/pdf/current")
            }
            is ArticleListTab -> {
                navigate(navigationHostController, "tab/article/list")
            }
            is ArticleTab -> {
                navigate(navigationHostController, "tab/article/content/${tab.title()}")
            }
            is CalendarTab -> {
                navigate(navigationHostController, "tool/calendar")
            }
            is EditorTab -> {
                navigate(navigationHostController, "tab/editor/current")
            }
        }
    }

    private fun refreshThumbnail() {
        CoroutineScope(Dispatchers.Default).launch(disposables) {
            runOnUiThread {
                val view = findViewById<View>(android.R.id.content) ?: return@runOnUiThread
                tabs.saveNewThumbnail(view)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
        GlobalWebViewPool.onResume()

        tabs.setCount()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        ClippingUrlOpener()(this) { browserViewModel?.open(it) }
    }

    /**
     * Refresh toolbar and background.
     */
    private fun refresh() {
        val colorPair = preferenceApplier.colorPair()
        WindowOptionColorApplier()(window, colorPair)

        RecentAppColoringUseCase(
            ::getString,
            { resources },
            ::setTaskDescription,
            Build.VERSION.SDK_INT
        ).invoke(preferenceApplier.color)

        updateColorFilter()
        this.backgroundPath?.value = preferenceApplier.backgroundImagePath
    }

    private fun updateColorFilter() {
        this.filterColor?.value = preferenceApplier.useColorFilter()
    }

    /**
     * Open PDF from storage.
     */
    private fun openPdfTabFromStorage() {
        requestPermissionForOpenPdfTab.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    /**
     * Open Editor tab.
     */
    private fun openEditorTab(path: String? = null) {
        tabs.openNewEditorTab(path)
        replaceToCurrentTab()
    }

    private fun openTabOnBackground(title: String, url: String) {
        val callback = tabs.openBackgroundTab(title, url)
        contentViewModel?.snackWithAction(
            getString(R.string.message_tab_open_background, title),
            getString(R.string.open)
        ) {
            callback()
            replaceToCurrentTab(true)
        }
    }

    /**
     * Action on empty tabs.
     */
    private fun onEmptyTabs() {
        contentViewModel?.switchTabList()
        openNewTab()
    }

    private fun onClickClear() {
        tabs.clear()
        onEmptyTabs()
    }

    override fun onCloseOnly() {
        coroutineScope?.launch {
            contentViewModel?.hideBottomSheet()
        }
    }

    override fun onCloseTabListDialogFragment(lastTabId: String) {
        if (lastTabId != tabs.currentTabId()) {
            replaceToCurrentTab()
        }
    }

    override fun onOpenEditor() = openEditorTab()

    override fun onOpenPdf() = openPdfTabFromStorage()

    override fun openNewTabFromTabList() {
        openNewTab()
    }

    private fun openNewTab() {
        when (StartUp.findByName(preferenceApplier.startUp)) {
            StartUp.SEARCH -> {
                navigate(navigationHostController, "search/top")
            }
            StartUp.BROWSER -> {
                tabs.openNewWebTab()
                replaceToCurrentTab(true)
            }
            StartUp.BOOKMARK -> {
                navigate(navigationHostController, "web/bookmark/list")
            }
        }
    }

    override fun tabIndexFromTabList() = tabs.index()

    override fun currentTabIdFromTabList() = tabs.currentTabId()

    override fun replaceTabFromTabList(tab: Tab) {
        tabs.replace(tab)
    }

    override fun getTabByIndexFromTabList(position: Int): Tab? = tabs.getTabByIndex(position)

    override fun closeTabFromTabList(position: Int) {
        tabs.closeTab(position)
    }

    override fun getTabAdapterSizeFromTabList(): Int = tabs.size()

    override fun swapTabsFromTabList(from: Int, to: Int) = tabs.swap(from, to)

    override fun tabIndexOfFromTabList(tab: Tab): Int = tabs.indexOf(tab)

    override fun onPause() {
        super.onPause()
        tabs.saveTabList()
        GlobalWebViewPool.onPause()
    }

    override fun onDestroy() {
        tabs.dispose()
        disposables.cancel()
        GlobalWebViewPool.dispose()
        activityResultLauncher?.unregister()
        requestPermissionForOpenPdfTab.unregister()
        downloadPermissionRequestLauncher.unregister()
        unregisterReceiver(musicPlayerBroadcastReceiver)
        super.onDestroy()
    }

}
