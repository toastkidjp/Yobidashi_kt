/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.main

import android.net.Uri
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import jp.toastkid.article_viewer.article.detail.ContentViewerFragment
import jp.toastkid.yobidashi.browser.BrowserFragment
import jp.toastkid.yobidashi.browser.BrowserFragmentViewModel
import jp.toastkid.yobidashi.editor.EditorFragment
import jp.toastkid.yobidashi.pdf.PdfViewerFragment
import jp.toastkid.yobidashi.tab.TabAdapter
import jp.toastkid.yobidashi.tab.model.ArticleTab
import jp.toastkid.yobidashi.tab.model.EditorTab
import jp.toastkid.yobidashi.tab.model.PdfTab
import jp.toastkid.yobidashi.tab.model.WebTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * @author toastkidjp
 */
class TabReplacingUseCase(
        private val tabs: TabAdapter,
        private val obtainFragment: (Class<out Fragment>) -> Fragment,
        private val replaceFragment: (Fragment, Boolean) -> Unit,
        private val browserFragmentViewModel: BrowserFragmentViewModel,
        private val refreshThumbnail: () -> Unit,
        private val runOnUiThread: (() -> Unit) -> Unit,
        private val disposables: Job
) {

    /**
     * Replace visibilities for current tab.
     *
     * @param withAnimation for suppress redundant animation.
     */
    operator fun invoke(withAnimation: Boolean = true) {
        when (val currentTab = tabs.currentTab()) {
            is WebTab -> {
                val browserFragment =
                        (obtainFragment(BrowserFragment::class.java) as? BrowserFragment) ?: return
                replaceFragment(browserFragment, false)
                CoroutineScope(Dispatchers.Default).launch(disposables) {
                    runOnUiThread {
                        browserFragmentViewModel
                                .loadWithNewTab(currentTab.getUrl().toUri() to currentTab.id())
                    }
                }
            }
            is EditorTab -> {
                val editorFragment =
                        obtainFragment(EditorFragment::class.java) as? EditorFragment ?: return
                editorFragment.arguments = bundleOf("path" to currentTab.path)
                replaceFragment(editorFragment, withAnimation)
                CoroutineScope(Dispatchers.Default).launch(disposables) {
                    runOnUiThread {
                        editorFragment.reload()
                        refreshThumbnail()
                    }
                }
            }
            is PdfTab -> {
                val url: String = currentTab.getUrl()
                if (url.isNotEmpty()) {
                    try {
                        val uri = Uri.parse(url)

                        val pdfViewerFragment =
                                obtainFragment(PdfViewerFragment::class.java) as? PdfViewerFragment
                                        ?: return
                        pdfViewerFragment.setInitialArguments(uri, currentTab.getScrolled())
                        replaceFragment(pdfViewerFragment, withAnimation)
                        refreshThumbnail()
                    } catch (e: SecurityException) {
                        Timber.e(e)
                        return
                    } catch (e: IllegalStateException) {
                        Timber.e(e)
                        return
                    }
                }
            }
            is ArticleTab -> {
                val fragment = obtainFragment(ContentViewerFragment::class.java)
                replaceFragment(fragment, withAnimation)
                CoroutineScope(Dispatchers.Default).launch(disposables) {
                    runOnUiThread {
                        (fragment as? ContentViewerFragment)?.loadContent(currentTab.title())
                        refreshThumbnail()
                    }
                }
            }
        }

        tabs.saveTabList()
    }

}