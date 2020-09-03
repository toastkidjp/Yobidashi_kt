/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.main

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import jp.toastkid.yobidashi.tab.tab_list.TabListDialogFragment

/**
 * @author toastkidjp
 */
class TabListUseCase(
        private val fragmentManager: FragmentManager,
        private val thumbnailRefresher: () -> Unit
) {

    /**
     * Tab list dialog fragment.
     */
    private var tabListDialogFragment: DialogFragment = TabListDialogFragment()

    fun switch() {
        if (tabListDialogFragment.isVisible) {
            dismiss()
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
        thumbnailRefresher()
        tabListDialogFragment.show(fragmentManager, TabListDialogFragment::class.java.canonicalName)
    }

    fun onBackPressed(): Boolean {
        if (tabListDialogFragment.isVisible) {
            tabListDialogFragment.dismiss()
            return true
        }
        return false
    }

}