/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.fragment

import android.databinding.DataBindingUtil
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentSettingDisplayBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.settings.background.BackgroundSettingActivity

/**
 * @author toastkidjp
 */
class DisplayingSettingFragment : Fragment(), TitleIdSupplier {

    private lateinit var binding: FragmentSettingDisplayBinding

    private lateinit var preferenceApplier: PreferenceApplier

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setting_display, container, false)
        val activityContext = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(activityContext)
        binding.fragment = this
        return binding.root
    }

    /**
     * Call background setting.
     *
     * @param view
     */
    fun backgroundSettings(view: View) {
        activity?.let {
            startActivity(BackgroundSettingActivity.makeIntent(it))
        }
    }

    /**
     * Clear background setting.
     *
     * @param view
     */
    fun clearBackgroundSettings(view: View) {
        preferenceApplier.removeBackgroundImagePath()
        Toaster.snackShort(
                binding.root,
                R.string.message_reset_bg_image,
                preferenceApplier.colorPair()
        )
    }

    override fun titleId() = R.string.title_settings_display
}