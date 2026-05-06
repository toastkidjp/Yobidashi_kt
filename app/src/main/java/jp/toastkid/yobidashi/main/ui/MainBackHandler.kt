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
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.navigation.Screen
import jp.toastkid.web.webview.GlobalWebViewPool

@Composable
internal fun MainBackHandler(
    currentRoute: () -> Screen?,
    navigationPopBackStack: () -> Unit,
    closeTab: () -> Unit,
    currentTabIsWeb: () -> Boolean,
    tabsIsEmpty: () -> Boolean
) {
    val activity = LocalActivity.current as? ComponentActivity ?: return
    val contentViewModel = viewModel(ContentViewModel::class.java, activity)

    BackHandler(true) {
        val route = currentRoute()

        if (route is Screen.Web) {
            val latest = GlobalWebViewPool.getLatest()
            if (latest?.canGoBack() == true) {
                latest.goBack()
                return@BackHandler
            }
        }

        navigationPopBackStack()

        if (route is Screen.Settings) {
            contentViewModel.refresh()
        }

        if (route?.isTab == true) {
            closeTab()

            if (tabsIsEmpty()) {
                contentViewModel.switchTabList()
                contentViewModel.openNewTab()
                return@BackHandler
            }
            contentViewModel.replaceToCurrentTab()
            return@BackHandler
        }

        if (route is Screen.Search && currentTabIsWeb().not()) {
            contentViewModel.replaceToCurrentTab()
            return@BackHandler
        }

        if (route is Screen.Empty || route == null) {
            activity.finish()
        }
    }
}