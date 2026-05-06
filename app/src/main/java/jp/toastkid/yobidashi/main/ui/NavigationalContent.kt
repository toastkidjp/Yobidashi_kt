/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main.ui

import android.view.View
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.net.toUri
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
import jp.toastkid.navigation.Screen
import jp.toastkid.number.NumberPlaceUi
import jp.toastkid.pdf.view.PdfViewerUi
import jp.toastkid.rss.view.RssReaderListUi
import jp.toastkid.search.favorite.FavoriteSearchListUi
import jp.toastkid.search.history.SearchHistoryListUi
import jp.toastkid.search.view.SearchInputUi
import jp.toastkid.sensor.presentation.view.SensorSwitcherView
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
    navigationHostController: MutableList<Screen>,
    tabs: TabAdapter
) {
    /*
                slideInHorizontally(initialOffsetX = { it })
     */
    /*
    enterTransition = {
            slideInVertically(initialOffsetY = { it })
        },
        popEnterTransition = {
            slideInVertically(initialOffsetY = { it })
        }
     */
    AnimatedContent(
        targetState = navigationHostController.last(),
        transitionSpec = {
            when {
                targetState.isTab -> {
                    slideInVertically(initialOffsetY = { it }) togetherWith
                            slideOutVertically { -it } + fadeOut()
                }
                targetState is Screen.Search
                        || targetState is Screen.SearchTop -> {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                }
                else -> {
                    fadeIn() togetherWith fadeOut()
                }
            }
        },
    ) { screen ->
        when (screen) {
            is Screen.Empty, Screen.Home -> {

            }
            is Screen.Web -> {
                val currentTab = tabs.currentTab() as? WebTab ?: return@AnimatedContent
                WebTabUi(currentTab.getUrl().toUri(), currentTab.id())
            }
            is Screen.Pdf -> {
                val currentTab = tabs.currentTab() as? PdfTab ?: return@AnimatedContent
                PdfViewerUi(
                    currentTab.getUrl().toUri(),
                    Modifier
                )
            }
            is Screen.ArticleList -> ArticleListUi()
            is Screen.Article -> {
                ArticleContentUi(screen.title, Modifier)
            }
            is Screen.Editor -> {
                val currentTab = tabs.currentTab() as? EditorTab ?: return@AnimatedContent
                val view = LocalView.current

                EditorTabView(currentTab.path, currentTab.getScrolled(), Modifier)
            }
            is Screen.WebBookmark -> BookmarkListUi()
            is Screen.WebHistory -> ViewHistoryListUi()
            is Screen.WebArchive -> ArchiveListUi()
            is Screen.BarcodeReader -> BarcodeReaderUi()
            is Screen.ImageViewer -> ImageListUi()
            is Screen.RssReader -> RssReaderListUi()
            is Screen.NumberPlace -> NumberPlaceUi()
            is Screen.TaskList -> TaskListUi()
            is Screen.TaskBoard -> TaskBoardUi()
            is Screen.LoanCalculator -> LoanCalculatorUi()
            is Screen.Calendar -> CalendarUi()
            is Screen.Settings -> SettingTopUi()
            is Screen.SearchTop -> SearchInputUi()
            is Screen.Search -> {
                SearchInputUi(screen.query, screen.title, screen.url)
            }
            is Screen.SearchHistory -> SearchHistoryListUi()
            is Screen.FavoriteSearch -> FavoriteSearchListUi()
            is Screen.AboutThisApp -> AboutThisAppUi(BuildConfig.VERSION_NAME)
            is Screen.ConverterTool -> ConverterToolUi()
            is Screen.Chat -> ChatTabView()
            is Screen.Sensor -> SensorSwitcherView()
            is Screen.WorldTime -> WorldTimeView()
        }
    }
}

private fun takeScreenshot(tabs: TabAdapter, view: View) {
    view.post {
        tabs.saveNewThumbnail(view)
    }
}
