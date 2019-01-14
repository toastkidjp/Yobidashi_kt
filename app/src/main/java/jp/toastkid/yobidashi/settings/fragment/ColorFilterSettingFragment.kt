/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.fragment

import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.color_filter.ColorFilter
import jp.toastkid.yobidashi.databinding.FragmentSettingSectionColorFilterBinding
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.settings.ColorFilterSettingInitializer

/**
 * @author toastkidjp
 */
class ColorFilterSettingFragment : Fragment(), TitleIdSupplier {

    private lateinit var binding: FragmentSettingSectionColorFilterBinding

    private lateinit var preferenceApplier: PreferenceApplier

    /**
     * Color filter.
     */
    private lateinit var colorFilter: ColorFilter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setting_section_color_filter, container, false)
        val activityContext = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(activityContext)

        activity?.let {
            colorFilter = ColorFilter(it, binding.root)
        }

        ColorFilterSettingInitializer(
                binding,
                { colorFilter.color(it) }
        ).invoke()
        binding.fragment = this
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val filterColor = preferenceApplier.filterColor()
        binding.sample.setBackgroundColor(filterColor)
        binding.alpha.setProgress(Color.alpha(filterColor))
        binding.useColorFilterCheck.isChecked = preferenceApplier.useColorFilter()
    }

    /**
     * Switch color filter's visibility.

     * @param v
     */
    fun switchColorFilter(v: View) {
        activity?.let {
            binding.useColorFilterCheck.isChecked = colorFilter.switchState(it)
        }
    }

    override fun titleId() = R.string.title_color_filter
}