/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivitySettingsBinding
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
class SettingFragment : Fragment() {

    /**
     * DataBinding object.
     */
    private lateinit var binding: ActivitySettingsBinding

    private lateinit var preferenceApplier: PreferenceApplier

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = context ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(context)

        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        binding.fragment = this

        childFragmentManager.let { fragmentManager ->
            binding.container.adapter = PagerAdapter(fragmentManager) { getString(it) }
            binding.container.offscreenPageLimit = 3
            binding.tabStrip.setupWithViewPager(binding.container)
        }

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.container.currentItem = 0
    }

    override fun onResume() {
        super.onResume()

        val colorPair = preferenceApplier.colorPair()

        binding.tabStrip.also {
            it.setBackgroundColor(colorPair.bgColor())
            it.tabTextColors = ColorStateList.valueOf(colorPair.fontColor())
            it.setSelectedTabIndicatorColor(colorPair.fontColor())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.setting_tab_shortcut, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item?.itemId) {
        R.id.menu_search -> {
            binding.container.currentItem = 2
            true
        }
        R.id.menu_browser -> {
            binding.container.currentItem = 3
            true
        }
        R.id.menu_editor -> {
            binding.container.currentItem = 4
            true
        }
        R.id.menu_notification -> {
            binding.container.currentItem = 6
            true
        }
        R.id.menu_other -> {
            binding.container.currentItem = 7
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID = R.layout.activity_settings

    }
}