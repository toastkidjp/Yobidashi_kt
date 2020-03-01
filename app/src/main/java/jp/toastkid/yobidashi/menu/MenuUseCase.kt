/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.menu

import android.content.ActivityNotFoundException
import android.content.Intent
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.journeyapps.barcodescanner.camera.CameraManager
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.about.AboutThisAppActivity
import jp.toastkid.yobidashi.barcode.BarcodeReaderActivity
import jp.toastkid.yobidashi.browser.BrowserFragment
import jp.toastkid.yobidashi.browser.BrowserViewModel
import jp.toastkid.yobidashi.launcher.LauncherActivity
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.intent.SettingsIntentFactory
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.main.ContentScrollable
import jp.toastkid.yobidashi.media.image.ImageViewerFragment
import jp.toastkid.yobidashi.media.music.popup.MediaPlayerPopup
import jp.toastkid.yobidashi.planning_poker.PlanningPokerActivity
import jp.toastkid.yobidashi.rss.RssReaderFragment
import jp.toastkid.yobidashi.settings.SettingsActivity
import jp.toastkid.yobidashi.torch.Torch
import jp.toastkid.yobidashi.wikipedia.today.DateArticleUrlFactory
import timber.log.Timber
import java.util.*

/**
 * @author toastkidjp
 */
class MenuUseCase(
        private val activitySupplier: () -> FragmentActivity,
        private val findCurrentFragment: () -> CommonFragmentAction?,
        private val replaceFragment: (Fragment) -> Unit,
        private val cleanProcess: () -> Unit,
        private val obtainFragment: (Class<out Fragment>) -> Fragment,
        private val openPdfTabFromStorage: () -> Unit,
        private val openEditorTab: () -> Unit,
        private val switchTabList: () -> Unit,
        private val useCameraPermission: (() -> Unit) -> Unit,
        private val close: () -> Unit
) {

    private val preferenceApplier = PreferenceApplier(activitySupplier())

    /**
     * Torch API facade.
     */
    private val torch by lazy {
        val fragmentActivity = activitySupplier()
        Torch(CameraManager(fragmentActivity)) {
            Toaster.snackShort(
                    fragmentActivity.findViewById(android.R.id.content),
                    it,
                    preferenceApplier.colorPair()
            )
        }
    }

    private val mediaPlayerPopup by lazy { MediaPlayerPopup(activitySupplier()) }

    fun onMenuClick(menu: Menu) {
        when (menu) {
            Menu.TOP-> {
                val currentFragment = findCurrentFragment()
                if (currentFragment is ContentScrollable) {
                    currentFragment.toTop()
                }
            }
            Menu.BOTTOM-> {
                val currentFragment = findCurrentFragment()
                if (currentFragment is ContentScrollable) {
                    currentFragment.toBottom()
                }
            }
            Menu.SETTING-> {
                startActivity(SettingsActivity.makeIntent(activitySupplier()))
            }
            Menu.SHARE-> {
                findCurrentFragment()?.share()
            }
            Menu.WIFI_SETTING-> {
                startActivity(SettingsIntentFactory.wifi())
            }
            Menu.CODE_READER -> {
                startActivity(BarcodeReaderActivity.makeIntent(activitySupplier()))
            }
            Menu.SCHEDULE-> {
                try {
                    startActivity(IntentFactory.makeCalendar())
                } catch (e: ActivityNotFoundException) {
                    Timber.w(e)
                }
            }
            /*Menu.OVERLAY_COLOR_FILTER-> {
                val rootView = binding.root
                ColorFilter(this, rootView).switchState(this)
            }*/
            Menu.MEMORY_CLEANER -> {
                cleanProcess()
            }
            Menu.PLANNING_POKER-> {
                startActivity(PlanningPokerActivity.makeIntent(activitySupplier()))
            }
            Menu.CAMERA-> {
                useCameraPermission { startActivity(IntentFactory.camera()) }
            }
            Menu.TORCH-> {
                useCameraPermission { torch.switch() }
            }
            Menu.APP_LAUNCHER-> {
                startActivity(LauncherActivity.makeIntent(activitySupplier()))
            }
            Menu.RSS_READER -> {
                replaceFragment(obtainFragment(RssReaderFragment::class.java))
            }
            Menu.AUDIO -> {
                mediaPlayerPopup.show(activitySupplier().findViewById(android.R.id.content))
                close()
            }
            Menu.ABOUT-> {
                startActivity(AboutThisAppActivity.makeIntent(activitySupplier()))
            }
            Menu.EXIT-> {
                activitySupplier().moveTaskToBack(true)
            }
            Menu.IMAGE_VIEWER -> {
                replaceFragment(obtainFragment(ImageViewerFragment::class.java))
            }
            Menu.LOAD_HOME-> {
                ViewModelProviders.of(activitySupplier()).get(BrowserViewModel::class.java)
                        .open(preferenceApplier.homeUrl.toUri())
            }
            Menu.EDITOR-> {
                openEditorTab()
            }
            Menu.PDF-> {
                openPdfTabFromStorage()
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

                ViewModelProviders.of(activitySupplier()).get(BrowserViewModel::class.java)
                        .open(url.toUri())
            }
            Menu.TAB_LIST-> {
                switchTabList()
            }
            else -> {
                (obtainFragment(BrowserFragment::class.java) as? BrowserFragment)
                        ?.onMenuClick(menu)
            }
        }
    }

    /**
     * Callback method on long clicked menu.
     *
     * @param menu
     * @return true
     */
    fun onMenuLongClick(menu: Menu): Boolean {
        Toaster.snackLong(
                // TODO extract to function.
                activitySupplier().findViewById(android.R.id.content),
                menu.titleId,
                R.string.run,
                View.OnClickListener { onMenuClick(menu) },
                preferenceApplier.colorPair()
        )
        return true
    }

    private fun startActivity(intent: Intent) {
        activitySupplier().startActivity(intent)
    }

    fun dispose() {
        torch.dispose()
    }

}