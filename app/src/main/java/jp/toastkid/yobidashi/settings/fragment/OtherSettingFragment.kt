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
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.appwidget.search.Updater
import jp.toastkid.yobidashi.databinding.FragmentSettingOtherBinding
import jp.toastkid.yobidashi.libs.HtmlCompat
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.intent.SettingsIntentFactory
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.main.StartUp

/**
 * @author toastkidjp
 */
class OtherSettingFragment : Fragment(), TitleIdSupplier {

    /**
     * View Data Binding object.
     */
    private lateinit var binding: FragmentSettingOtherBinding

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setting_other, container, false)
        val activityContext = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(activityContext)
        binding.fragment = this

        binding.startUpItems.startUpSelector.setOnCheckedChangeListener { _, checkedId ->
            preferenceApplier.startUp = StartUp.findById(checkedId)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.startUpItems.startUpSelector.let {
            it.check(preferenceApplier.startUp.radioButtonId)
            it.jumpDrawablesToCurrentState()
        }
        binding.wifiOnlyCheck.let {
            it.isChecked = preferenceApplier.wifiOnly
            it.jumpDrawablesToCurrentState()
        }
    }

    /**
     * Switch Wi-Fi only mode.
     */
    fun switchWifiOnly() {
        val newState = !preferenceApplier.wifiOnly
        preferenceApplier.wifiOnly = newState
        binding.wifiOnlyCheck.isChecked = newState
    }


    /**
     * Clear all settings.
     *
     * @param v
     */
    fun clearSettings(v: View) {
        val fragmentActivity = activity ?: return
        AlertDialog.Builder(fragmentActivity)
                .setTitle(R.string.title_clear_settings)
                .setMessage(HtmlCompat.fromHtml(getString(R.string.confirm_clear_all_settings)))
                .setCancelable(true)
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .setPositiveButton(R.string.ok) { d, _ ->
                    preferenceApplier.clear()
                    //TODO colorFilter.stop()
                    //TODO setCurrentValues()
                    Updater.update(fragmentActivity)
                    Toaster.snackShort(v, R.string.done_clear, preferenceApplier.colorPair())
                    d.dismiss()
                }
                .show()
    }

    /**
     * Call device settings.
     */
    fun deviceSetting() {
        startActivity(SettingsIntentFactory.makeLaunch())
    }

    /**
     * Call Wi-Fi settings.
     */
    fun wifi() {
        startActivity(SettingsIntentFactory.wifi())
    }

    /**
     * Call Wireless settings.
     */
    fun wireless() {
        startActivity(SettingsIntentFactory.wireless())
    }

    /**
     * Call Date-and-Time settings.
     */
    fun dateAndTime() {
        startActivity(SettingsIntentFactory.dateAndTime())
    }

    /**
     * Call display settings.
     */
    fun display() {
        startActivity(SettingsIntentFactory.display())
    }

    /**
     * Call all app settings.
     */
    fun allApps() {
        startActivity(SettingsIntentFactory.allApps())
    }

    @StringRes
    override fun titleId() = R.string.subhead_others

}