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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.swiperefresh.SwipeRefreshNestedScrollConnection
import jp.toastkid.rss.extractor.RssUrlFinder
import jp.toastkid.ui.dialog.ConfirmDialog
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.FaviconApplier
import jp.toastkid.yobidashi.browser.WebViewContainer
import jp.toastkid.yobidashi.browser.bookmark.BookmarkInsertion
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.browser.shortcut.ShortcutUseCase
import jp.toastkid.yobidashi.browser.usecase.PrintCurrentPageUseCase
import jp.toastkid.yobidashi.browser.user_agent.UserAgentDropdown
import jp.toastkid.yobidashi.browser.view.dialog.AnchorLongTapDialog
import jp.toastkid.yobidashi.browser.view.dialog.PageInformationDialog
import jp.toastkid.yobidashi.browser.view.reader.ReaderModeUi
import jp.toastkid.yobidashi.browser.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.libs.network.NetworkChecker
import jp.toastkid.yobidashi.tab.model.WebTab
import jp.toastkid.yobidashi.wikipedia.random.RandomWikipedia
import kotlinx.coroutines.launch
import kotlin.math.min

@Composable
internal fun WebTabUi(webTab: WebTab) {
    val activityContext = LocalContext.current as? ComponentActivity ?: return

    val coroutineScope = rememberCoroutineScope()
    val browserViewModel = remember { BrowserViewModel() }
    val webViewContainer = remember {
        WebViewContainer(activityContext, browserViewModel, coroutineScope)
    }

    val contentViewModel = viewModel(ContentViewModel::class.java, activityContext)

    val refreshTriggerPx = with(LocalDensity.current) { 96.dp.toPx() }
    val verticalIndicatorOffsetPx = with(LocalDensity.current) { -24.dp.toPx() }.toInt()
    LaunchedEffect(browserViewModel.swipeRefreshState.value?.isSwipeInProgress) {
        if (browserViewModel.swipeRefreshState.value?.isSwipeInProgress == false) {
            // If there's not a swipe in progress, rest the indicator at 0f
            browserViewModel.swipeRefreshState.value?.animateOffsetTo(0f)
        }
    }
    val nestedScrollConnection = SwipeRefreshNestedScrollConnection(
        browserViewModel.swipeRefreshState.value,
        coroutineScope
    ) {
        if (browserViewModel.swipeRefreshState.value?.isRefreshing == false) {
            contentViewModel.showAppBar(coroutineScope)
            webViewContainer.reload()
            browserViewModel.swipeRefreshState.value?.isRefreshing = true
        }
        browserViewModel.swipeRefreshState.value?.isSwipeInProgress = false
    }.also {
        it.refreshTrigger = refreshTriggerPx
        it.enabled = true
    }

    Box(
        modifier = Modifier.nestedScroll(nestedScrollConnection)
    ) {
        AndroidView(
            factory = {
                webViewContainer.loadWithNewTab(webTab.latest.url().toUri(), webTab.id())
                webViewContainer.view()
            },
            modifier = Modifier
                .nestedScroll(
                    connection = object : NestedScrollConnection {},
                    dispatcher = webViewContainer.nestedScrollDispatcher()
                )
        )

        if (browserViewModel.showSwipeRefreshIndicator()) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            0,
                            min(
                                verticalIndicatorOffsetPx + (browserViewModel.swipeRefreshState.value?.indicatorOffset?.toInt()
                                    ?: 0),
                                nestedScrollConnection.refreshTrigger.toInt()
                            )
                        )
                    }
                    .alpha(
                        browserViewModel.calculateSwipeRefreshIndicatorAlpha(refreshTriggerPx)
                    )
                    .align(Alignment.TopCenter)
            ) {
                CircularProgressIndicator(
                    progress = browserViewModel.calculateSwipingProgress(refreshTriggerPx),
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }

    val readerModeText = remember { mutableStateOf("") }
    if (readerModeText.value.isNotBlank()) {
        ReaderModeUi(webViewContainer.currentTitle(), readerModeText)
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
        browserViewModel.openLongTapDialog.value = true

        AnchorLongTapDialog(
            value.first,
            value.second,
            value.third
        ) {
            browserViewModel.openLongTapDialog.value = false
            browserViewModel?.clearLongTapParameters()
        }
    }

    BackHandler(readerModeText.value.isNotBlank()) {
        if (readerModeText.value.isNotBlank()) {
            readerModeText.value = ""
        }
    }

    LaunchedEffect(key1 = LocalLifecycleOwner.current, block = {
        contentViewModel.event.collect {
            webViewContainer.useEvent(it)
        }
    })

    val focusManager = LocalFocusManager.current
    LaunchedEffect(key1 = LocalLifecycleOwner.current, block = {
        webViewContainer.refresh()
        browserViewModel.initializeSwipeRefreshState(refreshTriggerPx)

        focusManager.clearFocus(true)

        contentViewModel.replaceAppBarContent {
            AppBarContent(
                browserViewModel,
                webViewContainer
            ) {
                readerModeText.value = if (readerModeText.value.isNotEmpty()) "" else it
            }
        }
    })

    val storagePermissionRequestLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) {
                contentViewModel.snackShort(R.string.message_requires_permission_storage)
                return@rememberLauncherForActivityResult
            }

            webViewContainer.downloadAllImages()
        }

    LaunchedEffect(key1 = "add_option_menu", block = {
        contentViewModel.optionMenus(
            OptionMenu(titleId = R.string.translate, action = {
                val language = activityContext.resources.configuration.locales[0].language
                val source = if (language == "en") "ja" else "en"
                contentViewModel.open(
                    ("https://papago.naver.net/website?locale=auto&source=${source}&target=$language&url="
                            + Uri.encode(webViewContainer.currentUrl()))
                        .toUri()
                )
            }),
            OptionMenu(titleId = R.string.download_all_images, action = {
                storagePermissionRequestLauncher
                    .launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }),
            OptionMenu(titleId = R.string.add_to_home_screen, action = {
                val shortcutUri = webViewContainer.currentUrl()?.toUri() ?: return@OptionMenu
                ShortcutUseCase(activityContext)
                    .invoke(
                        shortcutUri,
                        webViewContainer.currentTitle(),
                        FaviconApplier(activityContext).load(shortcutUri)
                    )
            }),
            OptionMenu(titleId = R.string.title_add_bookmark, action = {
                val faviconApplier = FaviconApplier(activityContext)
                val url = webViewContainer.currentUrl() ?: ""
                BookmarkInsertion(
                    activityContext,
                    webViewContainer.currentTitle(),
                    url,
                    faviconApplier.makePath(url),
                    Bookmark.getRootFolderName()
                ).insert()

                contentViewModel.snackShort(R.string.message_done_added_bookmark)
            }),
            OptionMenu(titleId = R.string.title_print_page, action = {
                PrintCurrentPageUseCase().invoke(GlobalWebViewPool.getLatest())
            }),
            OptionMenu(titleId = R.string.title_archive, action = {
                webViewContainer.saveArchive()
            }),
            OptionMenu(titleId = R.string.title_add_to_rss_reader, action = {
                RssUrlFinder(
                    PreferenceApplier(activityContext),
                    contentViewModel
                )
                    .invoke(webViewContainer.currentUrl())
            }),
            OptionMenu(titleId = R.string.title_replace_home, action = {
                webViewContainer.currentUrl()?.let {
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
                    NetworkChecker().isUnavailableWiFi(activityContext)
                ) {
                    contentViewModel.snackShort(R.string.message_wifi_not_connecting)
                    return@OptionMenu
                }

                RandomWikipedia()
                    .fetchWithAction { title, link ->
                        contentViewModel.open(link)
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
@Composable
private fun AppBarContent(
    viewModel: BrowserViewModel,
    browserModule: WebViewContainer,
    resetReaderModeContent: (String) -> Unit
) {
    val activity = LocalContext.current as? ComponentActivity ?: return

    val contentViewModel = viewModel(ContentViewModel::class.java, activity)

    val openPageInformation = remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .height(76.dp)
            .fillMaxWidth()
    ) {
        if (viewModel.progress.value < 70) {
            LinearProgressIndicator(
                progress = viewModel.progress.value.toFloat() / 100f,
                color = MaterialTheme.colorScheme.onPrimary,
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
                MaterialTheme.colorScheme.onPrimary,
                viewModel.enableBack.value
            ) { browserModule.back() }

            HeaderSubButton(
                R.drawable.ic_forward,
                R.string.title_menu_forward,
                MaterialTheme.colorScheme.onPrimary,
                viewModel.enableForward.value
            ) { browserModule.forward() }

            HeaderSubButton(
                R.drawable.ic_reader_mode,
                R.string.title_menu_reader_mode,
                MaterialTheme.colorScheme.onPrimary
            ) {
                browserModule.invokeContentExtraction {
                    showReader(it, contentViewModel, resetReaderModeContent)
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .padding(8.dp)
                    .combinedClickable(
                        true,
                        onClick = { contentViewModel.switchTabList() },
                        onLongClick = { contentViewModel.openNewTab() }
                    )
            ) {
                Image(
                    painterResource(R.drawable.ic_tab),
                    contentDescription = stringResource(id = R.string.tab_list),
                    colorFilter = ColorFilter.tint(
                        MaterialTheme.colorScheme.onPrimary,
                        BlendMode.SrcIn
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                )
                Text(
                    text = "${contentViewModel.tabCount.value}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                )
            }

            HeaderSubButton(
                R.drawable.ic_bookmark,
                R.string.title_bookmark,
                MaterialTheme.colorScheme.onPrimary
            ) { contentViewModel.nextRoute("web/bookmark/list") }

            HeaderSubButton(
                R.drawable.ic_history,
                R.string.title_view_history,
                MaterialTheme.colorScheme.onPrimary
            ) { contentViewModel.nextRoute("web/history/list") }

            Box {
                val open = remember { mutableStateOf(false) }
                HeaderSubButton(
                    R.drawable.ic_user_agent,
                    R.string.title_user_agent,
                    MaterialTheme.colorScheme.onPrimary
                ) { open.value = true }
                UserAgentDropdown(open) {
                    PreferenceApplier(activity).setUserAgent(it.name)
                    browserModule.resetUserAgent(it.text())
                    contentViewModel.snackShort(
                        activity.getString(
                            R.string.format_result_user_agent,
                            it.title()
                        )
                    )
                }
            }

            HeaderSubButton(
                R.drawable.ic_info,
                R.string.title_menu_page_information,
                MaterialTheme.colorScheme.onPrimary
            ) {
                openPageInformation.value = true
            }

            if (openPageInformation.value) {
                val pageInformation = browserModule.makeCurrentPageInformation()
                if (pageInformation.isEmpty.not()) {
                    PageInformationDialog(openPageInformation, pageInformation)
                }
            }

            HeaderSubButton(
                R.drawable.ic_home,
                R.string.title_load_home,
                MaterialTheme.colorScheme.onPrimary
            ) {
                contentViewModel.open(PreferenceApplier(activity).homeUrl.toUri())
            }

            HeaderSubButton(
                R.drawable.ic_code,
                R.string.title_menu_html_source,
                MaterialTheme.colorScheme.onPrimary
            ) {
                browserModule.invokeHtmlSourceExtraction {
                    val replace = it.replace("\\u003C", "<")
                    showReader(replace, contentViewModel, resetReaderModeContent)
                }
            }
        }

        Box(modifier = Modifier.padding(start = 4.dp)) {
            AsyncImage(
                model = R.drawable.url_box_background,
                contentDescription = stringResource(id = R.string.search)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(32.dp)
                    .fillMaxWidth()
                    .clickable {
                        contentViewModel.webSearch()
                    }
            ) {
                AsyncImage(
                    model = viewModel.icon.value,
                    contentDescription = stringResource(id = R.string.image),
                    modifier = Modifier
                        .size(36.dp)
                        .padding(end = 8.dp)
                        .clickable { openPageInformation.value = true }
                )
                BrowserTitle(
                    viewModel.title.value,
                    viewModel.url.value,
                    viewModel.progress.value,
                    Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                )

                val isNotLoading = 70 < viewModel.progress.value
                val reloadIconId =
                    if (isNotLoading) R.drawable.ic_reload else R.drawable.ic_close
                Icon(
                    painterResource(id = reloadIconId),
                    contentDescription = stringResource(id = R.string.title_menu_reload),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .clickable {
                            if (isNotLoading) {
                                browserModule.reload()
                            } else {
                                browserModule.stopLoading()
                                coroutineScope.launch {
                                    viewModel.stopProgress(true)
                                }
                            }
                        }
                )
            }
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
