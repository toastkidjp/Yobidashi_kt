/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.TabListViewModel
import jp.toastkid.yobidashi.browser.webview.GlobalWebViewPool

@Composable
internal fun MainBackHandler(
    closeMenuIfNeed: () -> Boolean,
    closeBottomSheetIfNeed: () -> Boolean,
    closeFindInPageIfNeed: () -> Boolean,
    currentRoute: () -> String?,
    navigationPopBackStack: () -> Unit,
    closeTab: () -> Unit,
    tabsIsEmpty: () -> Boolean
) {
    val activity = LocalContext.current as? ComponentActivity ?: return
    val contentViewModel = viewModel(ContentViewModel::class.java, activity)
    val tabListViewModel = viewModel(TabListViewModel::class.java, activity)

    BackHandler(true) {
        if (closeMenuIfNeed()) {
            return@BackHandler
        }

        if (closeBottomSheetIfNeed()) {
            return@BackHandler
        }

        if (closeFindInPageIfNeed()) {
            return@BackHandler
        }

        val route = currentRoute()

        if (route?.startsWith("tab/web") == true) {
            val latest = GlobalWebViewPool.getLatest()
            if (latest?.canGoBack() == true) {
                latest.goBack()
                return@BackHandler
            }
        }

        navigationPopBackStack()

        if (route == "setting/top") {
            contentViewModel.refresh()
        }

        if (route?.startsWith("tab/") == true) {
            closeTab()

            if (tabsIsEmpty()) {
                contentViewModel.switchTabList()
                tabListViewModel.openNewTab()
                return@BackHandler
            }
            contentViewModel.replaceToCurrentTab()
            return@BackHandler
        }

        if (route == "empty" || route == null) {
            activity.finish()
        }
    }
}