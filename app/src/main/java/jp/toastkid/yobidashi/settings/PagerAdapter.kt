/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import jp.toastkid.yobidashi.settings.fragment.*

/**
 * Setting fragments pager adapter.
 *
 * @param fragmentManager [FragmentManager]
 * @param titleResolver Title string resource resolve consumer.
 * @author toastkidjp
 */
class PagerAdapter(
        fragmentManager: FragmentManager,
        private val titleResolver: (Int) -> String
) : FragmentPagerAdapter(fragmentManager) {

    private val browserSettingFragment by lazy { BrowserSettingFragment() }

    private val searchSettingFragment by lazy { SearchSettingFragment() }

    private val colorFilterSettingFragment by lazy { ColorFilterSettingFragment() }

    private val displayingSettingFragment by lazy { DisplayingSettingFragment() }

    private val editorSettingFragment by lazy { EditorSettingFragment() }

    private val notificationSettingFragment by lazy { NotificationSettingFragment() }

    private val otherSettingFragment by lazy { OtherSettingFragment() }

    override fun getCount(): Int = 7

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> displayingSettingFragment
        1 -> searchSettingFragment
        2 -> browserSettingFragment
        3 -> editorSettingFragment
        4 -> colorFilterSettingFragment
        5 -> notificationSettingFragment
        6 -> otherSettingFragment
        else -> otherSettingFragment
    }

    override fun getPageTitle(position: Int): CharSequence? {
        val item = getItem(position)
        return if (item is TitleIdSupplier) titleResolver(item.titleId()) else ""
    }
}