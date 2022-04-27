/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.view

import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.TabListViewModel
import jp.toastkid.lib.intent.ShareIntentFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.viewmodel.PageSearcherViewModel
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserFragmentViewModel
import jp.toastkid.yobidashi.browser.BrowserHeaderViewModel
import jp.toastkid.yobidashi.browser.BrowserModule
import jp.toastkid.yobidashi.browser.LoadingViewModel
import jp.toastkid.yobidashi.browser.user_agent.UserAgentDropdown
import jp.toastkid.yobidashi.browser.webview.usecase.WebViewAssignmentUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun WebTabUi(webViewAssignmentUseCase: WebViewAssignmentUseCase, uri: Uri, tabId: String? = null) {
/*TODO swipe refresher
        binding?.swipeRefresher?.let {
            it.setOnRefreshListener { reload() }
            it.setOnChildScrollUpCallback { _, _ -> browserModule.disablePullToRefresh() }
            it.setDistanceToTriggerSync(500)
        }
*/
    val activityContext = LocalContext.current as? ComponentActivity ?: return

    val webViewContainer = remember { FrameLayout(activityContext) }
    webViewContainer.background = ColorDrawable(Color.Transparent.toArgb())
    val enableBackStack = remember { mutableStateOf(true) }
    val browserModule = BrowserModule(activityContext, webViewContainer)

    initializeHeaderViewModels(activityContext, browserModule)

    AndroidView(
        factory = {
            webViewContainer
        },
        update = {
            tabId?.let {
                browserModule.loadWithNewTab(uri, tabId)
            }
        },
        modifier = Modifier
            .background(Color.Transparent)
            .verticalScroll(ScrollState(0))
    )
    BackHandler(enableBackStack.value) {
        if (browserModule.back()) {
            enableBackStack.value = browserModule.canGoBack()
            return@BackHandler
        }
        enableBackStack.value = false
    }

    val contentViewModel = ViewModelProvider(activityContext).get(ContentViewModel::class.java)
    contentViewModel.toTop.observe(activityContext, {
        it.getContentIfNotHandled() ?: return@observe
        browserModule.pageUp()
    })
    contentViewModel.toBottom.observe(activityContext, {
        it.getContentIfNotHandled() ?: return@observe
        browserModule.pageDown()
    })
    contentViewModel.share.observe(activityContext, {
        it.getContentIfNotHandled() ?: return@observe
        activityContext.startActivity(
            ShareIntentFactory()(browserModule.makeShareMessage())
        )
    })
}

@OptIn(ExperimentalFoundationApi::class)
private fun initializeHeaderViewModels(activity: ComponentActivity, browserModule: BrowserModule) {
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
                        modifier = Modifier.height(1.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
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
                    HeaderSubButton(
                        R.drawable.ic_info,
                        R.string.title_menu_page_information,
                        tint
                    ) {
                        val pageInformation = browserModule.makeCurrentPageInformation()
                        if (pageInformation.isEmpty) {
                            return@HeaderSubButton
                        }

                        /*TODO Implement compose PageInformationDialogFragment()
                            .also { it.arguments = pageInformation }
                            .show(
                                parentFragmentManager,
                                PageInformationDialogFragment::class.java.simpleName
                            )*/
                    }
                    HeaderSubButton(
                        R.drawable.ic_code,
                        R.string.title_menu_html_source,
                        tint
                    ) {
                        browserModule.invokeHtmlSourceExtraction {
                            showReaderFragment(it.replace("\\u003C", "<")) {
                                contentViewModel.snackShort("This page can't show reader mode.")
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(32.dp)
                        .fillMaxWidth()
                        .clickable { tapHeader(activity, browserModule, preferenceApplier) }
                    //url_box_background
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_reader_mode),
                        contentDescription = stringResource(id = R.string.title_menu_reader_mode),
                        tint = tint,
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable {
                                //TODO
                                //browserModule.invokeContentExtraction(ValueCallback(this::showReaderFragment))
                            }
                    )
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        val progressTitle =
                            if (progress.value ?: 100 < 70)
                                activity.getString(R.string.prefix_loading) + "${progress.value}%"
                            else
                                headerTitle.value ?: ""

                        Text(
                            text = progressTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = tint,
                            fontSize = 12.sp
                        )
                        Text(
                            text = headerUrl.value ?: "",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = tint,
                            fontSize = 10.sp
                        )
                    }

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

    viewModelProvider.get(BrowserFragmentViewModel::class.java)
        .loadWithNewTab
        .observe(activity, {
            browserModule.loadWithNewTab(it.first, it.second)
        })

    viewModelProvider.get(PageSearcherViewModel::class.java).also { viewModel ->
        viewModel.find.observe(activity, Observer {
            browserModule.find(it)
        })

        viewModel.upward.observe(activity, Observer {
            browserModule.findUp()
        })

        viewModel.downward.observe(activity, Observer {
            browserModule.findDown()
        })
    }
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
            .width(40.dp)
            .padding(4.dp)
            .alpha(if (enable) 1f else 0.6f)
            .clickable(enabled = enable, onClick = onClick)
    )
}

/*override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
        R.id.translate -> {
            val activity = activity ?: return true
            val browserViewModel =
                ViewModelProvider(activity).get(BrowserViewModel::class.java)
            TranslatedPageOpenerUseCase(browserViewModel).invoke(browserModule.currentUrl())
        }
        R.id.download_all_images -> {
            storagePermissionRequestLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        R.id.add_to_home -> {
            val uri = browserModule.currentUrl()?.toUri() ?: return true
            val context = context ?: return true
            ShortcutUseCase(context)
                .invoke(
                    uri,
                    browserModule.currentTitle(),
                    FaviconApplier(context).load(uri)
                )
        }
        R.id.add_bookmark -> {
            val context = context ?: return true
            val faviconApplier = FaviconApplier(context)
            val url = browserModule.currentUrl() ?: ""
            BookmarkInsertion(
                context,
                browserModule.currentTitle(),
                url,
                faviconApplier.makePath(url),
                Bookmark.getRootFolderName()
            ).insert()

            contentViewModel?.snackShort(context.getString(R.string.message_done_added_bookmark))
            return true
        }
        R.id.add_archive -> {
            browserModule.saveArchive()
            return true
        }
        R.id.add_rss -> {
            RssUrlFinder(preferenceApplier).invoke(browserModule.currentUrl()) { null *//* TODO*//* }
                return true
            }
            R.id.replace_home -> {
                browserModule.currentUrl()?.let {
                    val context = context ?: return true
                    if (Urls.isInvalidUrl(it)) {
                        contentViewModel?.snackShort(context.getString(R.string.message_cannot_replace_home_url))
                        return true
                    }
                    preferenceApplier.homeUrl = it
                    contentViewModel?.snackShort(context.getString(R.string.message_replace_home_url, it))
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }*/

//TODO show composed reader UI
private fun showReaderFragment(content: String, snackShort: (String) -> Unit) {
    val cleaned = content.replace("^\"|\"$".toRegex(), "")
    if (cleaned.isBlank()) {
        snackShort("This page can't show reader mode.")
        return
    }

    val lineSeparator = System.lineSeparator()
    val replacedContent = cleaned.replace("\\n", lineSeparator)
}

/*TODO
        binding?.swipeRefresher?.let {
            it.setProgressBackgroundColorSchemeColor(preferenceApplier.color)
            it.setColorSchemeColors(preferenceApplier.fontColor)
        }*/

//TODO override fun pressBack(): Boolean = back()

/*
TODO share
startActivity(
        ShareIntentFactory()(browserModule.makeShareMessage())
    )
 */

private fun tapHeader(
    viewModelStoreOwner: ViewModelStoreOwner,
    browserModule: BrowserModule,
    preferenceApplier: PreferenceApplier
) {
    ViewModelProvider(viewModelStoreOwner)
        .get(ContentViewModel::class.java)
        .webSearch()
    /*
     val currentTitle = browserModule.currentTitle()
    val currentUrl = browserModule.currentUrl()
    val inputText = if (preferenceApplier.enableSearchQueryExtract) {
        SearchQueryExtractor()(currentUrl) ?: currentUrl
    } else {
        currentUrl
    }
    makeWithQuery(
        inputText ?: "",
        currentTitle,
        currentUrl
    )
     */
}

fun stopSwipeRefresherLoading() {
    //TODO binding?.swipeRefresher?.isRefreshing = false
}

/*override fun onPause() {
    browserModule.onPause()
}

override fun onDetach() {
    appBarViewModel?.show()
    browserModule.onDestroy()
}*/

