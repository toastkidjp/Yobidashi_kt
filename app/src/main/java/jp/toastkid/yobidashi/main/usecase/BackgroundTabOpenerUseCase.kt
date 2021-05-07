/*
 * Copyright (c) 2021 toastkidjp.
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

/**
 * @author toastkidjp
 */
class BackgroundTabOpenerUseCase(
        private val parent: View,
        private val openBackgroundTab: (String, String) -> () -> Unit,
        private val replaceToCurrentTab: () -> Unit
) {

    operator fun invoke(title: String, url: String, colorPair: ColorPair) {
        val callback = openBackgroundTab(title, url)
        val context = parent.context
        Toaster.withAction(
                parent,
                context.getString(R.string.message_tab_open_background, title),
                context.getString(R.string.open),
                {
                    callback()
                    replaceToCurrentTab()
                },
                colorPair,
                Snackbar.LENGTH_SHORT
        )
    }

}