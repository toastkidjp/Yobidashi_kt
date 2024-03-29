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
import jp.toastkid.web.webview.GlobalWebViewPool

@Composable
internal fun MainBackHandler(
    currentRoute: () -> String?,
    navigationPopBackStack: () -> Unit,
    closeTab: () -> Unit,
    currentTabIsWeb: () -> Boolean,
    tabsIsEmpty: () -> Boolean
) {
    val activity = LocalContext.current as? ComponentActivity ?: return
    val contentViewModel = viewModel(ContentViewModel::class.java, activity)

    BackHandler(true) {
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
                contentViewModel.openNewTab()
                return@BackHandler
            }
            contentViewModel.replaceToCurrentTab()
            return@BackHandler
        }

        if (route == "search/top" && currentTabIsWeb().not()) {
            contentViewModel.replaceToCurrentTab()
            return@BackHandler
        }

        if (route == "empty" || route == null) {
            activity.finish()
        }
    }
}