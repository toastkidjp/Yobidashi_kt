/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import jp.toastkid.yobidashi.settings.color.ColorSettingFragment
import jp.toastkid.yobidashi.settings.fragment.BrowserSettingFragment
import jp.toastkid.yobidashi.settings.fragment.ColorFilterSettingFragment
import jp.toastkid.yobidashi.settings.fragment.DisplayingSettingFragment
import jp.toastkid.yobidashi.settings.fragment.EditorSettingFragment
import jp.toastkid.yobidashi.settings.fragment.NotificationSettingFragment
import jp.toastkid.yobidashi.settings.fragment.OtherSettingFragment
import jp.toastkid.yobidashi.settings.fragment.SearchSettingFragment
import jp.toastkid.yobidashi.settings.fragment.TitleIdSupplier

/**
 * Setting fragments pager adapter.
 *
 * @param fragmentManager [FragmentManager]
 * @param titleResolver Title string resource resolve consumer.
 * @author toastkidjp
 */
class PagerAdapter(
        private val fragmentManager: FragmentManager,
        private val titleResolver: (Int) -> String
) : FragmentPagerAdapter(fragmentManager) {

    override fun getCount(): Int = 8

    override fun getItem(position: Int): Fragment = obtainFragment(
            when (position) {
                0 -> DisplayingSettingFragment::class.java
                1 -> ColorSettingFragment::class.java
                2 -> SearchSettingFragment::class.java
                3 -> BrowserSettingFragment::class.java
                4 -> EditorSettingFragment::class.java
                5 -> ColorFilterSettingFragment::class.java
                6 -> NotificationSettingFragment::class.java
                7 -> OtherSettingFragment::class.java
                else -> OtherSettingFragment::class.java
            }
    )

    private fun obtainFragment(fragmentClass: Class<out Fragment>) =
            fragmentManager.findFragmentByTag(fragmentClass.canonicalName)
                    ?: fragmentClass.newInstance()

    override fun getPageTitle(position: Int): CharSequence? {
        val item = getItem(position)
        return if (item is TitleIdSupplier) titleResolver(item.titleId()) else ""
    }

}