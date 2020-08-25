/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentSettingColorFilterBinding

/**
 * @author toastkidjp
 */
class ColorFilterSettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingColorFilterBinding

    private lateinit var preferenceApplier: PreferenceApplier

    private var overlayColorFilterViewModel: OverlayColorFilterViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        val activityContext = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(activityContext)

        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let { activity ->
            overlayColorFilterViewModel =
                    ViewModelProvider(activity).get(OverlayColorFilterViewModel::class.java)
            overlayColorFilterViewModel
                    ?.newColor
                    ?.observe(activity, Observer { binding.sample.setBackgroundColor(it) })
            binding.useCase = OverlayColorFilterUseCase(
                    preferenceApplier,
                    { ContextCompat.getColor(activity, it) },
                    overlayColorFilterViewModel
            )
        }

        binding.alpha.addOnChangeListener { _, value, _ ->
            binding.useCase?.setAlpha(value.toInt())
        }

        binding.defaultColor.setOnClickListener{
            binding.useCase?.setDefault()
            binding.alpha.value = OverlayColorFilterUseCase.getDefaultAlpha().toFloat()
        }
    }

    override fun onResume() {
        super.onResume()
        val filterColor = preferenceApplier.filterColor(ContextCompat.getColor(binding.root.context, R.color.default_color_filter))
        binding.sample.setBackgroundColor(filterColor)
        binding.alpha.value = Color.alpha(filterColor).toFloat()
        binding.useColorFilterCheck.let {
            it.isChecked = preferenceApplier.useColorFilter()
            it.jumpDrawablesToCurrentState()
            overlayColorFilterViewModel?.update()
        }
    }

    /**
     * Switch color filter's visibility.
     */
    fun switchColorFilter() {
        val newState = !preferenceApplier.useColorFilter()
        binding.useColorFilterCheck.isChecked = newState
        preferenceApplier.setUseColorFilter(newState)
        overlayColorFilterViewModel?.update()
    }

    companion object : TitleIdSupplier {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.fragment_setting_color_filter

        @StringRes
        override fun titleId() = R.string.title_color_filter

    }
}