/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.view.factory

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import jp.toastkid.yobidashi.browser.view.WebTabUi
import jp.toastkid.yobidashi.browser.webview.usecase.WebViewAssignmentUseCase

class WebTabUiFactory(private val webViewAssignmentUseCase: WebViewAssignmentUseCase) {

    @Composable
    operator fun invoke(uri: Uri, tabId: String? = null) {
        WebTabUi()
    }

    companion object {

        fun from(activity: ComponentActivity) =
            WebTabUiFactory(WebViewAssignmentUseCase.from(activity))

    }

}