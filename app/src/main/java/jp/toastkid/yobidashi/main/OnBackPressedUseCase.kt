/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.main

import androidx.fragment.app.FragmentManager
import jp.toastkid.article_viewer.article.detail.ContentViewerFragment
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserFragment
import jp.toastkid.yobidashi.browser.floating.FloatingPreview
import jp.toastkid.yobidashi.browser.page_search.PageSearcherModule
import jp.toastkid.yobidashi.editor.EditorFragment
import jp.toastkid.yobidashi.menu.MenuViewModel
import jp.toastkid.yobidashi.pdf.PdfViewerFragment
import jp.toastkid.yobidashi.tab.TabAdapter

/**
 * @author toastkidjp
 */
class OnBackPressedUseCase(
        private val tabListUseCase: TabListUseCase?,
        private val menuVisibility: () -> Boolean,
        private val menuViewModel: MenuViewModel?,
        private val pageSearcherModule: PageSearcherModule,
        private val floatingPreview: FloatingPreview?,
        private val tabs: TabAdapter,
        private val onEmptyTabs: () -> Unit,
        private val replaceToCurrentTab: TabReplacingUseCase,
        private val supportFragmentManager: FragmentManager
) {

    operator fun invoke() {
        if (tabListUseCase?.onBackPressed() == true) {
            return
        }

        if (menuVisibility()) {
            menuViewModel?.close()
            return
        }

        if (pageSearcherModule.isVisible()) {
            pageSearcherModule.hide()
            return
        }

        if (floatingPreview?.isVisible() == true) {
            floatingPreview.hide()
            return
        }

        val currentFragment = findFragment()
        if (currentFragment is CommonFragmentAction && currentFragment.pressBack()) {
            return
        }

        if (currentFragment is BrowserFragment
                || currentFragment is PdfViewerFragment
                || currentFragment is ContentViewerFragment
        ) {
            tabs.closeTab(tabs.index())

            if (tabs.isEmpty()) {
                onEmptyTabs()
                return
            }
            replaceToCurrentTab(true)
            return
        }

        val fragment = findFragment()
        if (fragment !is EditorFragment) {
            supportFragmentManager.popBackStackImmediate()
            return
        }

        confirmExit()
    }

    /**
     * Show confirm exit.
     */
    private fun confirmExit() {
        CloseDialogFragment()
                .show(supportFragmentManager, CloseDialogFragment::class.java.simpleName)
    }

    /**
     * Find current [Fragment].
     *
     * @return [Fragment]
     */
    private fun findFragment() = supportFragmentManager.findFragmentById(R.id.content)

}