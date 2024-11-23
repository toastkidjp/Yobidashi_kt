/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.web.view

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.ui.dialog.ConfirmDialog
import jp.toastkid.web.FaviconApplier
import jp.toastkid.web.R
import jp.toastkid.web.WebViewContainer
import jp.toastkid.web.bookmark.BookmarkInsertion
import jp.toastkid.web.rss.extractor.RssUrlFinder
import jp.toastkid.web.shortcut.ShortcutUseCase
import jp.toastkid.web.usecase.PrintCurrentPageUseCase
import jp.toastkid.web.user_agent.UserAgentDropdown
import jp.toastkid.web.view.dialog.AnchorLongTapDialog
import jp.toastkid.web.view.dialog.PageInformationDialog
import jp.toastkid.web.view.reader.ReaderModeUi
import jp.toastkid.web.view.refresh.SwipeRefreshNestedScrollConnection
import jp.toastkid.web.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebTabUi(uri: Uri, tabId: String) {
    val activityContext = LocalContext.current as? ComponentActivity ?: return

    val coroutineScope = rememberCoroutineScope()
    val browserViewModel = remember { WebTabUiViewModel() }

    val webViewContainer = remember {
        WebViewContainer(activityContext, browserViewModel)
    }

    val contentViewModel = viewModel(ContentViewModel::class.java, activityContext)

    val refreshTriggerPx = with(LocalDensity.current) { 96.dp.toPx() }

    val refreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = browserViewModel.isRefreshing(),
        state = refreshState,
        onRefresh = coroutineScope::class.java::getComponentType,
        indicator = {
            Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = browserViewModel.isRefreshing(),
                state = refreshState,
                containerColor = MaterialTheme.colorScheme.primary,
                color = MaterialTheme.colorScheme.onPrimary,
                threshold = 96.dp
            )
        },
        modifier = Modifier.nestedScroll(
            SwipeRefreshNestedScrollConnection(
                refreshState,
                coroutineScope,
                refreshTriggerPx,
                {
                    if (browserViewModel.isRefreshing()) {
                        return@SwipeRefreshNestedScrollConnection
                    }

                    coroutineScope.launch {
                        browserViewModel.setRefreshing()
                        contentViewModel.showAppBar(coroutineScope)
                        webViewContainer.reload()
                    }
                }
            )
        )
    ) {
        AndroidView(
            factory = {
                webViewContainer.loadWithNewTab(uri, tabId)
                webViewContainer.view()
            },
            modifier = Modifier
                .nestedScroll(
                    connection = object : NestedScrollConnection {},
                    dispatcher = webViewContainer.nestedScrollDispatcher()
                )
        )
    }

    if (browserViewModel.isOpenReaderMode()) {
        ReaderModeUi(
            webViewContainer.currentTitle(),
            browserViewModel.readerModeText(),
            browserViewModel::closeReaderMode
        )
    }

    if (browserViewModel.openErrorDialog()) {
        ConfirmDialog(
            stringResource(id = R.string.title_ssl_connection_error),
            browserViewModel.error(),
            onDismissRequest = browserViewModel::closeErrorDialog,
            onClickOk = browserViewModel::clearError
        )
    }

    if (browserViewModel.isOpenLongTapDialog()) {
        val value = browserViewModel.longTapActionParameters.value

        AnchorLongTapDialog(
            value.first,
            value.second,
            value.third,
            browserViewModel::clearLongTapParameters
        )
    }

    BackHandler(browserViewModel.isOpenReaderMode()) {
        browserViewModel.closeReaderMode()
    }

    val focusManager = LocalFocusManager.current
    LaunchedEffect(key1 = LocalLifecycleOwner.current, block = {
        webViewContainer.refresh()

        focusManager.clearFocus(true)

        contentViewModel.replaceAppBarContent {
            AppBarContent(
                browserViewModel,
                webViewContainer
            ) {
                if (browserViewModel.isOpenReaderMode()) {
                    browserViewModel.closeReaderMode()
                    return@AppBarContent
                }

                browserViewModel.showReader(it, contentViewModel)
            }
        }

        contentViewModel.event.collect(webViewContainer::useEvent)
    })

    val storagePermissionRequestLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) {
                contentViewModel.snackShort(jp.toastkid.lib.R.string.message_requires_permission_storage)
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
                        FaviconApplier(activityContext).load(shortcutUri),
                        activityContext::class.java.canonicalName ?: ""
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
            OptionMenu(titleId = R.string.title_archive, action = webViewContainer::saveArchive),
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
            })
        )
    })
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppBarContent(
    viewModel: WebTabUiViewModel,
    webViewContainer: WebViewContainer,
    openReaderMode: (String) -> Unit
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
        if (viewModel.shouldShowProgressIndicator()) {
            LinearProgressIndicator(
                progress = { viewModel.progress().toFloat() / 100f },
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
                viewModel.enableBack(),
                webViewContainer::back
            )

            HeaderSubButton(
                R.drawable.ic_forward,
                R.string.title_menu_forward,
                viewModel.enableForward(),
                webViewContainer::forward
            )

            HeaderSubButton(
                R.drawable.ic_reader_mode,
                R.string.title_menu_reader_mode
            ) {
                webViewContainer.invokeContentExtraction(openReaderMode)
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
                    painterResource(jp.toastkid.lib.R.drawable.ic_tab),
                    contentDescription = stringResource(id = jp.toastkid.lib.R.string.tab_list),
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
                jp.toastkid.lib.R.drawable.ic_bookmark,
                R.string.title_bookmark
            ) { contentViewModel.nextRoute("web/bookmark/list") }

            HeaderSubButton(
                R.drawable.ic_history,
                jp.toastkid.lib.R.string.title_view_history
            ) { contentViewModel.nextRoute("web/history/list") }

            Box {
                val open = remember { mutableStateOf(false) }
                HeaderSubButton(
                    R.drawable.ic_user_agent,
                    R.string.title_user_agent
                ) { open.value = true }
                UserAgentDropdown(open.value, { open.value = false }) {
                    PreferenceApplier(activity).setUserAgent(it.name)
                    webViewContainer.resetUserAgent(it.text())
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
                R.string.title_menu_page_information
            ) {
                openPageInformation.value = true
            }

            if (openPageInformation.value) {
                val pageInformation = webViewContainer.makeCurrentPageInformation()
                if (pageInformation.isEmpty.not()) {
                    PageInformationDialog(pageInformation, { openPageInformation.value = false })
                }
            }

            HeaderSubButton(
                R.drawable.ic_home,
                R.string.title_load_home
            ) {
                contentViewModel.open(PreferenceApplier(activity).homeUrl.toUri())
            }

            HeaderSubButton(
                R.drawable.ic_code,
                R.string.title_menu_html_source
            ) {
                webViewContainer.invokeHtmlSourceExtraction {
                    val replace = it.replace("\\u003C", "<")
                    openReaderMode(replace)
                }
            }
        }

        Box(modifier = Modifier.padding(start = 4.dp)) {
            AsyncImage(
                model = R.drawable.url_box_background,
                contentDescription = stringResource(id = jp.toastkid.lib.R.string.search)
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
                    model = viewModel.icon(),
                    contentDescription = stringResource(id = jp.toastkid.lib.R.string.image),
                    modifier = Modifier
                        .size(36.dp)
                        .padding(horizontal = 4.dp)
                        .clickable { openPageInformation.value = true }
                )
                TitleUrlBox(
                    viewModel.title(),
                    viewModel.url(),
                    viewModel.progress(),
                    Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                )

                val isNotLoading = !viewModel.shouldShowProgressIndicator()
                val reloadIconId =
                    if (isNotLoading) R.drawable.ic_reload else jp.toastkid.lib.R.drawable.ic_close
                Icon(
                    painterResource(id = reloadIconId),
                    contentDescription = stringResource(id = R.string.title_menu_reload),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .clickable {
                            if (isNotLoading) {
                                webViewContainer.reload()
                                return@clickable
                            }

                            webViewContainer.stopLoading()
                            coroutineScope.launch {
                                viewModel.stopProgress()
                            }
                        }
                )
            }
        }
    }
}

@Composable
private fun HeaderSubButton(
    iconId: Int,
    descriptionId: Int,
    enable: Boolean = true,
    onClick: () -> Unit
) {
    Icon(
        painterResource(id = iconId),
        contentDescription = stringResource(id = descriptionId),
        tint = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .width(44.dp)
            .padding(4.dp)
            .graphicsLayer { alpha = if (enable) 1f else 0.6f }
            .clickable(enabled = enable, onClick = onClick)
    )
}
