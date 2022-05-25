/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.ui.theme.AppTheme
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.permission.DownloadPermissionRequestContract
import jp.toastkid.yobidashi.browser.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.libs.clip.ClippingUrlOpener
import jp.toastkid.yobidashi.libs.network.DownloadAction
import jp.toastkid.yobidashi.main.ui.Content

class MainActivity : ComponentActivity() {

    private var browserViewModel: BrowserViewModel? = null

    private val downloadPermissionRequestLauncher =
        registerForActivityResult(DownloadPermissionRequestContract()) {
            if (it.first.not()) {
                ViewModelProvider(this).get(ContentViewModel::class.java)
                    .snackShort(R.string.message_requires_permission_storage)
                return@registerForActivityResult
            }
            val url = it.second ?: return@registerForActivityResult
            DownloadAction(this).invoke(url)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activityViewModelProvider = ViewModelProvider(this)

        browserViewModel = activityViewModelProvider.get(BrowserViewModel::class.java)
        browserViewModel?.download?.observe(this, Observer {
            val url = it?.getContentIfNotHandled() ?: return@Observer
            downloadPermissionRequestLauncher.launch(url)
        })

        processShortcut(intent)

        setContent {
            AppTheme(isSystemInDarkTheme() || PreferenceApplier(this).useDarkMode()) {
                Content()
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

    override fun onResume() {
        super.onResume()
        GlobalWebViewPool.onResume()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        ClippingUrlOpener()(this) { browserViewModel?.open(it) }
    }

    override fun onPause() {
        super.onPause()
        GlobalWebViewPool.onPause()
    }

    override fun onDestroy() {
        GlobalWebViewPool.dispose()
        downloadPermissionRequestLauncher.unregister()
        super.onDestroy()
    }

}
