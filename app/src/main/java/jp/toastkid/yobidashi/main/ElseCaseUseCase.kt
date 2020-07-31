/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.main

import androidx.fragment.app.Fragment
import jp.toastkid.lib.tab.TabUiFragment

/**
 * @author toastkidjp
 */
class ElseCaseUseCase(
        private val tabIsEmpty: () -> Boolean,
        private val openNewTab: () -> Unit,
        private val findCurrentFragment: () -> Fragment?,
        private val replaceToCurrentTab: (Boolean) -> Unit
) {

    operator fun invoke() {
        if (tabIsEmpty()) {
            openNewTab()
            return
        }

        // Add for re-creating activity.
        val currentFragment = findCurrentFragment()
        if (currentFragment is TabUiFragment || currentFragment == null) {
            replaceToCurrentTab(false)
        }
    }
}