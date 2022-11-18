/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.ui.theme.AppTheme
import jp.toastkid.yobidashi.browser.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.main.ui.Content

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        actionBar?.hide()

        setContent {
            val preferenceApplier = PreferenceApplier(this)

            val contentViewModel = viewModel(ContentViewModel::class.java, this)
            contentViewModel.initializeWith(preferenceApplier)

            AppTheme(
                contentViewModel.colorPair(),
                isSystemInDarkTheme() || preferenceApplier.useDarkMode()
            ) {
                Content()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        GlobalWebViewPool.onResume()
    }

    override fun onPause() {
        super.onPause()
        GlobalWebViewPool.onPause()
    }

    override fun onDestroy() {
        GlobalWebViewPool.dispose()
        super.onDestroy()
    }

}
