/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main.usecase

import android.view.View
import com.google.android.material.snackbar.Snackbar
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.tab.TabAdapter

class ArticleTabOpenerUseCase(
    private val tabs: TabAdapter,
    private val snackbarParent: View,
    private val replaceToCurrentTab: () -> Unit
) {

    operator fun invoke(title: String, background: Boolean, colorPair: ColorPair) {
        val tab = tabs.openNewArticleTab(title, background)
        if (background) {
            Toaster.withAction(
                snackbarParent,
                snackbarParent.context.getString(R.string.message_tab_open_background, title),
                snackbarParent.context.getString(R.string.open),
                {
                    tabs.replace(tab)
                    replaceToCurrentTab()
                },
                colorPair,
                Snackbar.LENGTH_SHORT
            )
        } else {
            replaceToCurrentTab()
        }
    }
}