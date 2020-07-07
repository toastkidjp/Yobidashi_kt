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
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentSettingsBinding
import jp.toastkid.yobidashi.libs.ad.AdService
import jp.toastkid.yobidashi.libs.ad.AdViewFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.ContentViewModel

/**
 * @author toastkidjp
 */
class SettingFragment : Fragment() {

    /**
     * DataBinding object.
     */
    private lateinit var binding: FragmentSettingsBinding

    private lateinit var preferenceApplier: PreferenceApplier

    private var adService: AdService? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val context = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(context)

        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        binding.fragment = this

        val pagerAdapter = PagerAdapter(this)
        binding.container.adapter = pagerAdapter
        binding.container.offscreenPageLimit = 3

        val mediator = TabLayoutMediator(binding.tabLayout, binding.container) { tab, position ->
            tab.text = pagerAdapter.getPageTitle(position)
        }
        mediator.attach()

        adService = AdService(AdViewFactory(), context)
        adService?.sendWith(ViewModelProvider(requireActivity()).get(AppBarViewModel::class.java))

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.container.currentItem = 0

        adService?.load()
    }

    override fun onResume() {
        super.onResume()

        val colorPair = preferenceApplier.colorPair()

        binding.tabLayout.also {
            it.setBackgroundColor(colorPair.bgColor())

            val fontColor = colorPair.fontColor()
            it.tabTextColors = ColorStateList.valueOf(fontColor)
            it.setSelectedTabIndicatorColor(fontColor)
        }
    }

    override fun onDetach() {
        ViewModelProvider(requireActivity()).get(ContentViewModel::class.java).refresh()
        super.onDetach()
    }

    override fun onDestroyView() {
        adService?.destroy()
        super.onDestroyView()
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID = R.layout.fragment_settings

    }
}