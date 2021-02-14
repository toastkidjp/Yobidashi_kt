/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.menu

import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.todo.view.board.BoardFragment
import jp.toastkid.todo.view.list.TaskListFragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.about.AboutThisAppFragment
import jp.toastkid.yobidashi.barcode.BarcodeReaderFragment
import jp.toastkid.yobidashi.browser.archive.ArchivesFragment
import jp.toastkid.yobidashi.browser.bookmark.BookmarkFragment
import jp.toastkid.yobidashi.browser.history.ViewHistoryFragment
import jp.toastkid.yobidashi.cleaner.ProcessCleanerInvoker
import jp.toastkid.yobidashi.gesture.GestureMemoFragment
import jp.toastkid.yobidashi.launcher.LauncherFragment
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.network.WifiConnectionChecker
import jp.toastkid.yobidashi.main.MainActivity
import jp.toastkid.yobidashi.media.image.list.ImageViewerFragment
import jp.toastkid.yobidashi.media.music.popup.MediaPlayerPopup
import jp.toastkid.yobidashi.planning_poker.CardListFragment
import jp.toastkid.yobidashi.rss.RssReaderFragment
import jp.toastkid.yobidashi.settings.fragment.OverlayColorFilterViewModel
import jp.toastkid.yobidashi.wikipedia.random.RandomWikipedia
import jp.toastkid.yobidashi.wikipedia.today.DateArticleUrlFactory
import java.util.Calendar

/**
 * TODO clean up duplicated codes.
 * @author toastkidjp
 */
class MenuUseCase(
        private val activitySupplier: () -> FragmentActivity,
        private val menuViewModel: MenuViewModel?
) {

    private val contentViewModel =
            ViewModelProvider(activitySupplier()).get(ContentViewModel::class.java)

    private val preferenceApplier = PreferenceApplier(activitySupplier())

    private val mediaPlayerPopup by lazy { MediaPlayerPopup(activitySupplier()) }

    fun observe() {
        val activity = activitySupplier()
        menuViewModel?.click?.observe(activity, Observer { event ->
            event.getContentIfNotHandled()?.let {
                onMenuClick(it)
            }
        })

        menuViewModel?.longClick?.observe(activity, Observer {
            onMenuLongClick(it)
        })
    }

    private fun onMenuClick(menu: Menu) {
        when (menu) {
            Menu.TOP-> {
                contentViewModel.toTop()
                return
            }
            Menu.BOTTOM-> {
                contentViewModel.toBottom()
                return
            }
            Menu.SHARE-> {
                contentViewModel.share()
            }
            Menu.CODE_READER -> {
                nextFragment(BarcodeReaderFragment::class.java)
            }
            Menu.OVERLAY_COLOR_FILTER-> {
                preferenceApplier.setUseColorFilter(preferenceApplier.useColorFilter().not())
                (activitySupplier() as? MainActivity)?.let {
                    ViewModelProvider(it).get(OverlayColorFilterViewModel::class.java)
                            .newColor(preferenceApplier.filterColor(ContextCompat.getColor(activitySupplier(), R.color.default_color_filter)))
                }
                return
            }
            Menu.MEMORY_CLEANER -> {
                val activity = activitySupplier()
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
                    contentViewModel.snackShort(activity.getString(R.string.message_cannot_use_under_l))
                    return
                }
                ProcessCleanerInvoker()(activity.findViewById(R.id.content))
            }
            Menu.PLANNING_POKER-> {
                nextFragment(CardListFragment::class.java)
            }
            Menu.APP_LAUNCHER-> {
                nextFragment(LauncherFragment::class.java)
            }
            Menu.RSS_READER -> {
                nextFragment(RssReaderFragment::class.java)
            }
            Menu.AUDIO -> {
                val parent = extractContentView() ?: return
                mediaPlayerPopup.show(parent)
                menuViewModel?.close()
            }
            Menu.BOOKMARK-> {
                nextFragment(BookmarkFragment::class.java)
            }
            Menu.VIEW_HISTORY-> {
                nextFragment(ViewHistoryFragment::class.java)
            }
            Menu.IMAGE_VIEWER -> {
                nextFragment(ImageViewerFragment::class.java)
            }
            Menu.LOAD_HOME-> {
                ViewModelProvider(activitySupplier()).get(BrowserViewModel::class.java)
                        .open(preferenceApplier.homeUrl.toUri())
            }
            Menu.EDITOR-> {
                contentViewModel.openEditorTab()
            }
            Menu.PDF-> {
                contentViewModel.openPdf()
            }
            Menu.CALENDAR -> {
                contentViewModel.openCalendar()
            }
            Menu.ARTICLE_VIEWER -> {
                contentViewModel.openArticleList()
            }
            Menu.WEB_SEARCH -> {
                contentViewModel.webSearch()
            }
            Menu.GESTURE_MEMO -> {
                nextFragment(GestureMemoFragment::class.java)
            }
            Menu.ABOUT_THIS_APP -> {
                nextFragment(AboutThisAppFragment::class.java)
            }
            Menu.TODO_TASKS_BOARD -> {
                nextFragment(BoardFragment::class.java)
            }
            Menu.TODO_TASKS -> {
                nextFragment(TaskListFragment::class.java)
            }
            Menu.WHAT_HAPPENED_TODAY -> {
                val calendar = Calendar.getInstance()
                val url = DateArticleUrlFactory()(
                        activitySupplier(),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                )
                if (Urls.isInvalidUrl(url)) {
                    return
                }

                ViewModelProvider(activitySupplier()).get(BrowserViewModel::class.java)
                        .open(url.toUri())
            }
            Menu.RANDOM_WIKIPEDIA -> {
                val activity = activitySupplier()
                if (preferenceApplier.wifiOnly &&
                        WifiConnectionChecker.isNotConnecting(activity)) {
                    contentViewModel.snackShort(R.string.message_wifi_not_connecting)
                    return
                }

                RandomWikipedia()
                        .fetchWithAction { title, link ->
                            ViewModelProvider(activitySupplier()).get(BrowserViewModel::class.java)
                                    .open(link)
                            val fragmentActivity = activitySupplier()
                            contentViewModel.snackShort(
                                    fragmentActivity.getString(R.string.message_open_random_wikipedia, title)
                            )
                        }
            }
            Menu.VIEW_ARCHIVE -> {
                nextFragment(ArchivesFragment::class.java)
            }
            Menu.FIND_IN_PAGE-> {
                contentViewModel.switchPageSearcher()
            }
        }
        menuViewModel?.close()
    }

    private fun nextFragment(fragmentClass: Class<out Fragment>) {
        contentViewModel.nextFragment(fragmentClass)
    }

    /**
     * Callback method on long clicked menu.
     *
     * @param menu
     * @return true
     */
    private fun onMenuLongClick(menu: Menu): Boolean {
        val view = extractContentView() ?: return true
        Toaster.snackLong(
                view,
                menu.titleId,
                R.string.run,
                View.OnClickListener { onMenuClick(menu) },
                preferenceApplier.colorPair()
        )
        return true
    }

    private fun extractContentView(): View? =
            activitySupplier().findViewById(R.id.content)

}