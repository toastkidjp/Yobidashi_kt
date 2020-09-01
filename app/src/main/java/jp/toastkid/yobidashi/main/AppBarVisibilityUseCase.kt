/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.main

import android.view.View
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.ScreenMode

/**
 * @author toastkidjp
 */
class AppBarVisibilityUseCase(
        private val appBar: View,
        private val preferenceApplier: PreferenceApplier
) {

    private val resources = appBar.resources
    
    fun show() =
            when (ScreenMode.find(preferenceApplier.browserScreenMode())) {
                ScreenMode.FIXED -> {
                    appBar.visibility = View.VISIBLE
                }
                ScreenMode.FULL_SCREEN -> Unit
                ScreenMode.EXPANDABLE -> appBar.animate()?.let {
                    it.cancel()
                    it.translationY(0f)
                            .setDuration(HEADER_HIDING_DURATION)
                            .withStartAction {
                                appBar.visibility = View.VISIBLE
                            }
                            .start()
                }
            }
    
    fun hide() =
            when (ScreenMode.find(preferenceApplier.browserScreenMode())) {
                ScreenMode.FIXED -> Unit
                ScreenMode.FULL_SCREEN -> {
                    appBar.visibility = View.GONE
                }
                ScreenMode.EXPANDABLE -> {
                    appBar.animate()?.let {
                        it.cancel()
                        it.translationY(-resources.getDimension(R.dimen.toolbar_height))
                                .setDuration(HEADER_HIDING_DURATION)
                                .withEndAction   {
                                    appBar.visibility = View.GONE
                                }
                                .start()
                    }
                }
            }
}

/**
 * Header hiding duration.
 */
private const val HEADER_HIDING_DURATION = 75L
