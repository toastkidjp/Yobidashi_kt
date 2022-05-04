/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.tab.tab_list

import jp.toastkid.yobidashi.tab.model.Tab

interface Callback {
    fun onCloseOnly()
    fun onCloseTabListDialogFragment(lastTabId: String)
    fun onOpenEditor()
    fun onOpenPdf()
    fun openNewTabFromTabList()
    fun tabIndexFromTabList(): Int
    fun currentTabIdFromTabList(): String
    fun replaceTabFromTabList(tab: Tab)
    fun getTabByIndexFromTabList(position: Int): Tab?
    fun closeTabFromTabList(position: Int)
    fun getTabAdapterSizeFromTabList(): Int
    fun swapTabsFromTabList(from: Int, to: Int)
    fun tabIndexOfFromTabList(tab: Tab): Int
}
