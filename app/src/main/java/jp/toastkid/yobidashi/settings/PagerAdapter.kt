/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import jp.toastkid.yobidashi.settings.color.ColorSettingFragment
import jp.toastkid.yobidashi.settings.fragment.BrowserSettingFragment
import jp.toastkid.yobidashi.settings.fragment.ColorFilterSettingFragment
import jp.toastkid.yobidashi.settings.fragment.DisplayingSettingFragment
import jp.toastkid.yobidashi.settings.fragment.EditorSettingFragment
import jp.toastkid.yobidashi.settings.fragment.NotificationSettingFragment
import jp.toastkid.yobidashi.settings.fragment.OtherSettingFragment
import jp.toastkid.yobidashi.settings.fragment.SearchSettingFragment
import jp.toastkid.yobidashi.settings.fragment.TitleIdSupplier
import timber.log.Timber

/**
 * Setting fragments pager adapter.
 *
 * @param fragment [Fragment]
 * @author toastkidjp
 */
class PagerAdapter(private val fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 8

    override fun createFragment(position: Int): Fragment = obtainFragment(pages[position].first)

    private fun obtainFragment(fragmentClass: Class<out Fragment>): Fragment {
        val f = fragment.childFragmentManager.findFragmentByTag(fragmentClass.canonicalName)
        if (f != null) {
            Timber.i("tomato found ${fragmentClass.canonicalName}")
            return f
        }
        Timber.i("tomato new ${fragmentClass.canonicalName}")
        return fragmentClass.newInstance()
    }

    fun getPageTitle(position: Int): CharSequence? {
        return getTitleIdByPosition(position)?.let { fragment.getString(it) } ?: ""
    }

    private fun getTitleIdByPosition(position: Int): Int? = pages[position].second.titleId()

    companion object {
        private val pages = listOf(
                DisplayingSettingFragment::class.java to DisplayingSettingFragment,
                ColorSettingFragment::class.java to ColorSettingFragment,
                SearchSettingFragment::class.java to SearchSettingFragment,
                BrowserSettingFragment::class.java to BrowserSettingFragment,
                EditorSettingFragment::class.java to EditorSettingFragment,
                ColorFilterSettingFragment::class.java to ColorFilterSettingFragment,
                NotificationSettingFragment::class.java to NotificationSettingFragment,
                OtherSettingFragment::class.java to OtherSettingFragment
        )
    }
}