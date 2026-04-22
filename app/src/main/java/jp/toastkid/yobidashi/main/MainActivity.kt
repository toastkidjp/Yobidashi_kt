/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.webkit.URLUtil
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.ui.theme.AppTheme
import jp.toastkid.web.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.main.ui.Content

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferenceApplier = PreferenceApplier(this)

        val contentViewModel = ViewModelProvider(this).get(ContentViewModel::class.java)
        contentViewModel.initializeWith(preferenceApplier)

        setContent {
            AppTheme(
                contentViewModel.colorPair(),
                isSystemInDarkTheme() || preferenceApplier.useDarkMode()
            ) {
                Content()
            }
        }

        // 起動時の Intent を処理
        handleIntent(intent)
    }

    // Activity が既に起動している状態で別の Intent を受け取った場合
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) {
            return
        }

        val url = when (intent.action) {
            Intent.ACTION_VIEW -> {
                intent.dataString
            }

            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    intent.getStringExtra(Intent.EXTRA_TEXT)
                } else null
            }

            "jp.toastkid.yobidashi.search", Intent.ACTION_ASSIST -> {
                // 必要に応じてクエリなどを URL に変換
                intent.getStringExtra(SearchManager.QUERY)?.let { "https://www.google.com/search?q=$it" }
            }

            else -> null
        }

        url?.let {
            if (URLUtil.isValidUrl(it)) {
                val contentViewModel = ViewModelProvider(this).get(ContentViewModel::class.java)
                contentViewModel.open(it.toUri())
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        GlobalWebViewPool.storeStates(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        GlobalWebViewPool.restoreStates(savedInstanceState)
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
