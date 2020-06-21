/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.tab.tab_list

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author toastkidjp
 */
class TabListService(
        private val fragmentManager: FragmentManager,
        private val thumbnailRefresher: () -> Unit,
        private val postDelayed: (Runnable, Long) -> Unit
) {

    /**
     * Tab list dialog fragment.
     */
    private var tabListDialogFragment: DialogFragment = TabListDialogFragment()

    private var isTabListShowing = AtomicBoolean(false)

    fun switch() {
        if (tabListDialogFragment.isVisible) {
            tabListDialogFragment.dismiss()
        } else {
            showTabList()
        }
    }

    fun dismiss() {
        tabListDialogFragment.dismiss()
    }

    /**
     * Show tab list.
     */
    private fun showTabList() {
        if (isTabListShowing.get()) {
            return
        }
        isTabListShowing.set(true)
        thumbnailRefresher()
        tabListDialogFragment.show(fragmentManager, TabListDialogFragment::class.java.canonicalName)
        postDelayed(Runnable { isTabListShowing.set(false) }, 1000L)
    }

    fun onBackPressed(): Boolean {
        if (tabListDialogFragment.isVisible) {
            tabListDialogFragment.dismiss()
            return true
        }
        return false
    }

}