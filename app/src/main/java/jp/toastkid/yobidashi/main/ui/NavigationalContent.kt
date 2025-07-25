/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.net.toUri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import jp.toastkid.about.view.AboutThisAppUi
import jp.toastkid.article_viewer.article.detail.view.ArticleContentUi
import jp.toastkid.article_viewer.article.list.view.ArticleListUi
import jp.toastkid.barcode.view.BarcodeReaderUi
import jp.toastkid.calendar.view.CalendarUi
import jp.toastkid.chat.presentation.ChatTabView
import jp.toastkid.converter.presentation.ui.ConverterToolUi
import jp.toastkid.editor.view.EditorTabView
import jp.toastkid.image.view.ImageListUi
import jp.toastkid.loan.view.LoanCalculatorUi
import jp.toastkid.number.NumberPlaceUi
import jp.toastkid.pdf.view.PdfViewerUi
import jp.toastkid.rss.view.RssReaderListUi
import jp.toastkid.search.favorite.FavoriteSearchListUi
import jp.toastkid.search.history.SearchHistoryListUi
import jp.toastkid.search.view.SearchInputUi
import jp.toastkid.setting.presentation.screen.SettingTopUi
import jp.toastkid.todo.view.board.TaskBoardUi
import jp.toastkid.todo.view.list.TaskListUi
import jp.toastkid.web.archive.view.ArchiveListUi
import jp.toastkid.web.bookmark.view.BookmarkListUi
import jp.toastkid.web.history.view.ViewHistoryListUi
import jp.toastkid.web.view.WebTabUi
import jp.toastkid.world.presentation.WorldTimeView
import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.tab.TabAdapter
import jp.toastkid.yobidashi.tab.model.EditorTab
import jp.toastkid.yobidashi.tab.model.PdfTab
import jp.toastkid.yobidashi.tab.model.WebTab

@Composable
internal fun NavigationalContent(
    navigationHostController: NavHostController,
    tabs: TabAdapter
) {
    NavHost(
        navController = navigationHostController,
        startDestination = "empty"
    ) {
        composable("empty")  {

        }
        tabComposable("tab/web/current") {
            val currentTab = tabs.currentTab() as? WebTab ?: return@tabComposable
            WebTabUi(currentTab.getUrl().toUri(), currentTab.id())
        }
        tabComposable("tab/pdf/current") {
            val currentTab = tabs.currentTab() as? PdfTab ?: return@tabComposable
            PdfViewerUi(
                currentTab.getUrl().toUri(),
                Modifier//.onPlaced { takeScreenshot(tabs, view) }
            )
        }
        tabComposable("tab/article/list") {
            ArticleListUi()
            takeScreenshot(tabs, LocalView.current)
        }
        tabComposable("tab/article/content/{title}") {
            val title = it?.getString("title") ?: return@tabComposable
            val view = LocalView.current
            ArticleContentUi(title, Modifier
                //.onPlaced { takeScreenshot(tabs, view) }
            )
        }
        tabComposable("tab/editor/current") {
            val currentTab = tabs.currentTab() as? EditorTab ?: return@tabComposable
            val view = LocalView.current

            EditorTabView(currentTab.path, Modifier
            //.onPlaced { takeScreenshot(tabs, view) }
            )
        }
        tabComposable("web/bookmark/list") {
            BookmarkListUi()
        }
        tabComposable("web/history/list") {
            ViewHistoryListUi()
        }
        composable("web/archive/list") {
            ArchiveListUi()
        }
        composable("tool/barcode_reader") {
            BarcodeReaderUi()
        }
        composable("tool/image/list") {
            ImageListUi()
        }
        composable("tool/rss/list") {
            RssReaderListUi()
        }
        composable("tool/number/place") {
            NumberPlaceUi()
        }
        composable("tool/task/list") {
            TaskListUi()
        }
        composable("tool/task/board") {
            TaskBoardUi()
        }
        composable("tool/loan") {
            LoanCalculatorUi()
        }
        composable("tab/calendar") {
            CalendarUi()
        }
        slideInComposable("setting/top") {
            SettingTopUi()
        }
        slideInComposable("search/top") {
            SearchInputUi()
        }
        slideInComposable("search/with/?query={query}&title={title}&url={url}") {
            val query = Uri.decode(it.getString("query"))
            val title = Uri.decode(it.getString("title"))
            val url = Uri.decode(it.getString("url"))
            SearchInputUi(query, title, url)
        }
        slideInComposable("search/history/list") {
            SearchHistoryListUi()
        }
        slideInComposable("search/favorite/list") {
            FavoriteSearchListUi()
        }
        composable("about") {
            AboutThisAppUi(BuildConfig.VERSION_NAME)
        }
        composable("tool/converter") {
            ConverterToolUi()
        }
        composable("tool/chat") {
            ChatTabView()
        }
        composable("tool/world_time") {
            WorldTimeView()
        }
    }
}

private fun takeScreenshot(tabs: TabAdapter, view: View) {
    view.post {
        tabs.saveNewThumbnail(view)
    }
}

private fun NavGraphBuilder.slideInComposable(route: String, content: @Composable (Bundle) -> Unit) {
    composable(
        route,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it })
        }
    ) {
        content(it.arguments ?: Bundle.EMPTY)
    }
}

private fun NavGraphBuilder.tabComposable(route: String, content: @Composable (Bundle?) -> Unit) {
    composable(
        route,
        enterTransition = {
            slideInVertically(initialOffsetY = { it })
        },
        popEnterTransition = {
            slideInVertically(initialOffsetY = { it })
        }
    ) {
        content(it.arguments)
    }
}