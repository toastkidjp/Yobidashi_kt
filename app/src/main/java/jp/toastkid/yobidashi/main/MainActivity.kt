/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.MutableState
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.WindowOptionColorApplier
import jp.toastkid.ui.theme.AppTheme
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.permission.DownloadPermissionRequestContract
import jp.toastkid.yobidashi.browser.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.libs.clip.ClippingUrlOpener
import jp.toastkid.yobidashi.libs.network.DownloadAction
import jp.toastkid.yobidashi.main.ui.Content
import jp.toastkid.yobidashi.settings.fragment.OverlayColorFilterViewModel
import kotlinx.coroutines.Job

class MainActivity : ComponentActivity() {

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    private var contentViewModel: ContentViewModel? = null

    private var browserViewModel: BrowserViewModel? = null

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

    private var filterColor: MutableState<Boolean>? = null

    private var backgroundPath: MutableState<String>? = null

    /**
     * Disposables.
     */
    private val disposables: Job by lazy { Job() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceApplier = PreferenceApplier(this)

        val activityViewModelProvider = ViewModelProvider(this)
        contentViewModel = activityViewModelProvider.get(ContentViewModel::class.java)
        contentViewModel?.refresh?.observe(this, {
            refresh()
        })

        browserViewModel = activityViewModelProvider.get(BrowserViewModel::class.java)
        browserViewModel?.download?.observe(this, Observer {
            val url = it?.getContentIfNotHandled() ?: return@Observer
            downloadPermissionRequestLauncher.launch(url)
        })

        activityViewModelProvider.get(OverlayColorFilterViewModel::class.java)
            .newColor
            .observe(this, {
                updateColorFilter()
            })

        registerReceiver(musicPlayerBroadcastReceiver, IntentFilter("jp.toastkid.music.action.open"))

        processShortcut(intent)

        setContent {
            AppTheme(isSystemInDarkTheme() || preferenceApplier.useDarkMode()) {
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
        refresh()
        GlobalWebViewPool.onResume()
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

    override fun onPause() {
        super.onPause()
        GlobalWebViewPool.onPause()
    }

    override fun onDestroy() {
        disposables.cancel()
        GlobalWebViewPool.dispose()
        downloadPermissionRequestLauncher.unregister()
        unregisterReceiver(musicPlayerBroadcastReceiver)
        super.onDestroy()
    }

}
