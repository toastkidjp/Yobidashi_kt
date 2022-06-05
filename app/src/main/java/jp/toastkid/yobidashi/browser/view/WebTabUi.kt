/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.view

import android.Manifest
import android.net.Uri
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.TabListViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.intent.ShareIntentFactory
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.viewmodel.PageSearcherViewModel
import jp.toastkid.rss.extractor.RssUrlFinder
import jp.toastkid.ui.dialog.ConfirmDialog
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserHeaderViewModel
import jp.toastkid.yobidashi.browser.BrowserModule
import jp.toastkid.yobidashi.browser.FaviconApplier
import jp.toastkid.yobidashi.browser.LoadingViewModel
import jp.toastkid.yobidashi.browser.bookmark.BookmarkInsertion
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.browser.shortcut.ShortcutUseCase
import jp.toastkid.yobidashi.browser.user_agent.UserAgentDropdown
import jp.toastkid.yobidashi.browser.view.dialog.AnchorLongTapDialog
import jp.toastkid.yobidashi.browser.view.dialog.PageInformationDialog
import jp.toastkid.yobidashi.browser.view.reader.ReaderModeUi
import jp.toastkid.yobidashi.browser.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.libs.network.NetworkChecker
import jp.toastkid.yobidashi.wikipedia.random.RandomWikipedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun WebTabUi(uri: Uri, tabId: String) {
    val activityContext = LocalContext.current as? ComponentActivity ?: return

    val webViewContainer = remember {
        val frameLayout = FrameLayout(activityContext)
        frameLayout.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        frameLayout
    }

    val browserModule = remember { BrowserModule(activityContext, webViewContainer) }
    browserModule.applyNewAlpha()
    browserModule.resizePool(PreferenceApplier(activityContext).poolSize)

    val browserHeaderViewModel =
        viewModel(modelClass = BrowserHeaderViewModel::class.java, activityContext)

    val browserViewModel = viewModel(BrowserViewModel::class.java, activityContext)
    val lifecycleOwner = LocalLifecycleOwner.current

    val scrollListener =
        View.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY == 0) {
                //TODO swipeRefreshState.isRefreshing = true
            }
            browserModule.nestedScrollDispatcher().dispatchPreScroll(
                Offset((oldScrollX - scrollX).toFloat(), (oldScrollY - scrollY).toFloat()),
                NestedScrollSource.Fling
            )
        }

    browserViewModel.switchWebViewToCurrent.observe(lifecycleOwner, {
        val newTabId = it?.getContentIfNotHandled() ?: return@observe
        browserModule.switchWebViewToCurrent(newTabId)
        GlobalWebViewPool.getLatest()?.setOnScrollChangeListener(scrollListener)
    })

    AndroidView(
        factory = {
            browserModule.loadWithNewTab(uri, tabId)
            GlobalWebViewPool.getLatest()?.setOnScrollChangeListener(scrollListener)
            webViewContainer
        },
        update = {
            GlobalWebViewPool.getLatest()?.setOnScrollChangeListener(scrollListener)
        },
        modifier = Modifier
            .nestedScroll(
                connection = object : NestedScrollConnection {},
                dispatcher = browserModule.nestedScrollDispatcher()
            )
    )

    val readerModeText = remember { mutableStateOf("") }
    if (readerModeText.value.isNotBlank()) {
        ReaderModeUi(browserModule.currentTitle(), readerModeText)
    }

    if (browserViewModel.openErrorDialog.value) {
        ConfirmDialog(
            browserViewModel.openErrorDialog,
            stringResource(id = R.string.title_ssl_connection_error),
            browserViewModel.error.value
        ) {
            browserViewModel.clearError()
        }
    }

    if (browserViewModel.openLongTapDialog.value) {
        val value = browserViewModel.longTapActionParameters.value
        AnchorLongTapDialog(
            browserViewModel.openLongTapDialog,
            value.first,
            value.second,
            value.third
        )
    }

    initializeHeaderViewModels(activityContext, browserModule) { readerModeText.value = it }

    BackHandler(browserHeaderViewModel.enableBackPress.value) {
        if (readerModeText.value.isNotBlank()) {
            readerModeText.value = ""
            return@BackHandler
        }
        if (browserModule.back()) {
            browserHeaderViewModel.setEnableBackPress(browserModule.canGoBack())
            return@BackHandler
        }
        browserHeaderViewModel.setEnableBackPress(false)
    }

    val contentViewModel = ViewModelProvider(activityContext).get(ContentViewModel::class.java)
    contentViewModel.toTop.observe(lifecycleOwner, {
        it.getContentIfNotHandled() ?: return@observe
        browserModule.pageUp()
    })
    contentViewModel.toBottom.observe(lifecycleOwner, {
        it.getContentIfNotHandled() ?: return@observe
        browserModule.pageDown()
    })
    contentViewModel.share.observe(lifecycleOwner, {
        it.getContentIfNotHandled() ?: return@observe
        activityContext.startActivity(
            ShareIntentFactory()(browserModule.makeShareMessage())
        )
    })

    val pageSearcherViewModel = viewModel(PageSearcherViewModel::class.java, activityContext)
    pageSearcherViewModel.also { viewModel ->
        viewModel.find.observe(lifecycleOwner, Observer {
            val word = it.getContentIfNotHandled() ?: return@Observer
            browserModule.find(word)
        })

        viewModel.upward.observe(lifecycleOwner, Observer {
            it.getContentIfNotHandled() ?: return@Observer
            browserModule.findUp()
        })

        viewModel.downward.observe(lifecycleOwner, Observer {
            it.getContentIfNotHandled() ?: return@Observer
            browserModule.findDown()
        })

        viewModel.clear.observe(lifecycleOwner, Observer {
            browserModule.clearMatches()
        })
    }

    val storagePermissionRequestLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) {
                contentViewModel.snackShort(R.string.message_requires_permission_storage)
                return@rememberLauncherForActivityResult
            }

            browserModule.downloadAllImages()
        }

    LaunchedEffect(key1 = "add_option_menu", block = {
        contentViewModel.optionMenus(
            OptionMenu(titleId = R.string.download_all_images, action = {
                storagePermissionRequestLauncher
                    .launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }),
            OptionMenu(titleId = R.string.add_to_home_screen, action = {
                val uri = browserModule.currentUrl()?.toUri() ?: return@OptionMenu
                ShortcutUseCase(activityContext)
                    .invoke(
                        uri,
                        browserModule.currentTitle(),
                        FaviconApplier(activityContext).load(uri)
                    )
            }),
            OptionMenu(titleId = R.string.title_add_bookmark, action = {
                val faviconApplier = FaviconApplier(activityContext)
                val url = browserModule.currentUrl() ?: ""
                BookmarkInsertion(
                    activityContext,
                    browserModule.currentTitle(),
                    url,
                    faviconApplier.makePath(url),
                    Bookmark.getRootFolderName()
                ).insert()

                contentViewModel.snackShort(R.string.message_done_added_bookmark)
            }),
            OptionMenu(titleId = R.string.title_archive, action = {
                browserModule.saveArchive()
            }),
            OptionMenu(titleId = R.string.title_add_to_rss_reader, action = {
                RssUrlFinder(
                    PreferenceApplier(activityContext),
                    contentViewModel
                )
                    .invoke(browserModule.currentUrl()) {
                        null//TODO
                    }
            }),
            OptionMenu(titleId = R.string.title_replace_home, action = {
                browserModule.currentUrl()?.let {
                    if (Urls.isInvalidUrl(it)) {
                        contentViewModel.snackShort(R.string.message_cannot_replace_home_url)
                        return@let
                    }
                    PreferenceApplier(activityContext).homeUrl = it
                    contentViewModel
                        .snackShort(activityContext.getString(R.string.message_replace_home_url, it))
                }
            }),
            OptionMenu(titleId = R.string.menu_random_wikipedia, action = {
                if (PreferenceApplier(activityContext).wifiOnly &&
                    NetworkChecker.isUnavailableWiFi(activityContext)
                ) {
                    contentViewModel.snackShort(R.string.message_wifi_not_connecting)
                    return@OptionMenu
                }

                RandomWikipedia()
                    .fetchWithAction { title, link ->
                        browserViewModel.open(link)
                        contentViewModel.snackShort(
                            activityContext.getString(
                                R.string.message_open_random_wikipedia,
                                title
                            )
                        )
                    }
            })
        )
    })
}

@OptIn(ExperimentalFoundationApi::class)
private fun initializeHeaderViewModels(
    activity: ComponentActivity,
    browserModule: BrowserModule,
    resetReaderModeContent: (String) -> Unit
) {
    val viewModelProvider = ViewModelProvider(activity)
    val appBarViewModel = viewModelProvider.get(AppBarViewModel::class.java)

    viewModelProvider.get(BrowserHeaderViewModel::class.java).also { viewModel ->
        viewModel.stopProgress.observe(activity, Observer {
            val stop = it?.getContentIfNotHandled() ?: return@Observer
            if (stop.not()
            //TODO || binding?.swipeRefresher?.isRefreshing == false
            ) {
                return@Observer
            }
            stopSwipeRefresherLoading()
        })
    }

    val tabListViewModel = viewModelProvider.get(TabListViewModel::class.java)
    val contentViewModel = viewModelProvider.get(ContentViewModel::class.java)
    val pageSearcherViewModel = viewModelProvider.get(PageSearcherViewModel::class.java)

    viewModelProvider.get(BrowserHeaderViewModel::class.java).also { viewModel ->
        appBarViewModel?.replace {
            val preferenceApplier = PreferenceApplier(activity)
            val tint = Color(preferenceApplier.fontColor)

            val headerTitle = viewModel.title.observeAsState()
            val headerUrl = viewModel.url.observeAsState()
            val progress = viewModel.progress.observeAsState()
            val enableBack = viewModel.enableBack.observeAsState()
            val enableForward = viewModel.enableForward.observeAsState()
            val tabCountState = tabListViewModel?.tabCount?.observeAsState()

            Column(
                modifier = Modifier
                    .height(76.dp)
                    .fillMaxWidth()
            ) {
                if (progress.value ?: 0 < 70) {
                    LinearProgressIndicator(
                        progress = (progress.value?.toFloat() ?: 100f) / 100f,
                        color = Color(preferenceApplier.fontColor),
                        modifier = Modifier
                            .height(1.dp)
                            .fillMaxWidth()
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    HeaderSubButton(
                        R.drawable.ic_back,
                        R.string.back,
                        tint,
                        enableBack.value ?: false
                    ) { browserModule.back() }
                    HeaderSubButton(
                        R.drawable.ic_forward,
                        R.string.title_menu_forward,
                        tint,
                        enableForward.value ?: false
                    ) { browserModule.forward() }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .padding(8.dp)
                            .combinedClickable(
                                true,
                                onClick = { contentViewModel?.switchTabList() },
                                onLongClick = { tabListViewModel?.openNewTab() }
                            )
                    ) {
                        Image(
                            painterResource(R.drawable.ic_tab),
                            contentDescription = stringResource(id = R.string.tab_list),
                            colorFilter = ColorFilter.tint(
                                Color(preferenceApplier.fontColor),
                                BlendMode.SrcIn
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                        )
                        Text(
                            text = "${tabCountState?.value ?: 0}",
                            color = Color(preferenceApplier.fontColor),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                        )
                    }
                    Box {
                        val open = remember { mutableStateOf(false) }
                        HeaderSubButton(
                            R.drawable.ic_user_agent,
                            R.string.title_user_agent,
                            tint
                        ) { open.value = true }
                        UserAgentDropdown(open) {
                            preferenceApplier.setUserAgent(it.name)
                            browserModule.resetUserAgent(it.text())
                            contentViewModel?.snackShort(
                                activity.getString(
                                    R.string.format_result_user_agent,
                                    it.title()
                                )
                            )
                        }
                    }

                    val openPageInformation = remember { mutableStateOf(false) }
                    HeaderSubButton(
                        R.drawable.ic_info,
                        R.string.title_menu_page_information,
                        tint
                    ) {
                        openPageInformation.value = true
                    }

                    if (openPageInformation.value) {
                        val pageInformation = browserModule.makeCurrentPageInformation()
                        if (pageInformation.isEmpty.not()) {
                            PageInformationDialog(openPageInformation, pageInformation)
                        }
                    }

                    val browserViewModel = viewModel(BrowserViewModel::class.java, activity)
                    HeaderSubButton(
                        R.drawable.ic_home,
                        R.string.title_load_home,
                        tint
                    ) {
                        browserViewModel
                            .open(preferenceApplier.homeUrl.toUri())
                    }

                    HeaderSubButton(
                        R.drawable.ic_code,
                        R.string.title_menu_html_source,
                        tint
                    ) {
                        browserModule.invokeHtmlSourceExtraction {
                            val replace = it.replace("\\u003C", "<")
                            showReader(replace, contentViewModel, resetReaderModeContent)
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(32.dp)
                        .fillMaxWidth()
                        .clickable {
                            ViewModelProvider(activity)
                                .get(ContentViewModel::class.java)
                                .webSearch()
                        }
                    //url_box_background
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_reader_mode),
                        contentDescription = stringResource(id = R.string.title_menu_reader_mode),
                        tint = tint,
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable {
                                browserModule.invokeContentExtraction {
                                    showReader(it, contentViewModel, resetReaderModeContent)
                                }
                            }
                    )

                    BrowserTitle(
                        progress,
                        headerTitle,
                        headerUrl,
                        Modifier.weight(1f)
                    )

                    val isNotLoading = 70 < progress.value ?: 100
                    val reloadIconId =
                        if (isNotLoading) R.drawable.ic_reload else R.drawable.ic_close
                    Icon(
                        painterResource(id = reloadIconId),
                        contentDescription = stringResource(id = R.string.title_menu_reload),
                        tint = tint,
                        modifier = Modifier
                            .clickable {
                                if (isNotLoading) {
                                    browserModule.reload()
                                } else {
                                    browserModule.stopLoading()
                                    stopSwipeRefresherLoading()
                                }
                            }
                    )
                }
            }
        }
    }

    CoroutineScope(Dispatchers.Main).launch {
        viewModelProvider.get(LoadingViewModel::class.java)
            .onPageFinished
            .collect {
                browserModule.saveArchiveForAutoArchive()
            }
    }
}

private fun showReader(
    content: String,
    contentViewModel: ContentViewModel,
    resetReaderModeContent: (String) -> Unit
) {
    val cleaned = content.replace("^\"|\"$".toRegex(), "")
    if (cleaned.isBlank()) {
        contentViewModel.snackShort("This page can't show reader mode.")
        return
    }

    val lineSeparator = System.lineSeparator()
    resetReaderModeContent(cleaned.replace("\\n", lineSeparator))
}

@Composable
private fun HeaderSubButton(
    iconId: Int,
    descriptionId: Int,
    tint: Color,
    enable: Boolean = true,
    onClick: () -> Unit
) {
    Icon(
        painterResource(id = iconId),
        contentDescription = stringResource(id = descriptionId),
        tint = tint,
        modifier = Modifier
            .width(44.dp)
            .padding(4.dp)
            .alpha(if (enable) 1f else 0.6f)
            .clickable(enabled = enable, onClick = onClick)
    )
}

/*TODO
        binding?.swipeRefresher?.let {
            it.setProgressBackgroundColorSchemeColor(preferenceApplier.color)
            it.setColorSchemeColors(preferenceApplier.fontColor)
        }*/

fun stopSwipeRefresherLoading() {
    //TODO binding?.swipeRefresher?.isRefreshing = false
}
