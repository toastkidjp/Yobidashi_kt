/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.SeekBar
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.CookieCleanerCompat
import jp.toastkid.yobidashi.browser.ScreenMode
import jp.toastkid.yobidashi.browser.user_agent.UserAgent
import jp.toastkid.yobidashi.browser.user_agent.UserAgentDialogFragment
import jp.toastkid.yobidashi.databinding.FragmentSettingBrowserBinding
import jp.toastkid.yobidashi.libs.TextInputs
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.menu.MenuPos

/**
 * Setting fragment of WEB browser.
 *
 * @author toastkidjp
 */
class BrowserSettingFragment : Fragment(), UserAgentDialogFragment.Callback, TitleIdSupplier {

    /**
     * Data Binding object.
     */
    private lateinit var binding: FragmentSettingBrowserBinding

    /**
     * Preference wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        val activityContext = context ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(activityContext)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.let {
            it.fragment = this
            TextInputs.setEmptyAlert(it.homeInputLayout)
        }
        initBrowserExpandable()
        initMenuPos()
    }

    override fun onResume() {
        super.onResume()
        setCurrentValues()
    }

    /**
     * Set current values to components.
     */
    private fun setCurrentValues() {
        binding.let {
            preferenceApplier.colorPair().setTo(it.homeButton)
            it.homeInputLayout.editText?.setText(preferenceApplier.homeUrl)

            it.retainTabsCheck.isChecked = preferenceApplier.doesRetainTabs()
            it.retainTabsCheck.jumpDrawablesToCurrentState()

            it.browserJsCheck.isChecked = preferenceApplier.useJavaScript()
            it.browserJsCheck.jumpDrawablesToCurrentState()

            it.useImageCheck.isChecked = preferenceApplier.doesLoadImage()
            it.useImageCheck.jumpDrawablesToCurrentState()

            it.saveFormCheck.isChecked = preferenceApplier.doesSaveForm()
            it.saveFormCheck.jumpDrawablesToCurrentState()

            it.userAgentValue.text = UserAgent.valueOf(preferenceApplier.userAgent()).title()

            it.saveViewHistoryCheck.isChecked = preferenceApplier.saveViewHistory
            it.saveViewHistoryCheck.jumpDrawablesToCurrentState()

            it.useInversionCheck.isChecked = preferenceApplier.useInversion
            it.useInversionCheck.jumpDrawablesToCurrentState()

            it.adRemoveCheck.isChecked = preferenceApplier.adRemove
            it.adRemoveCheck.jumpDrawablesToCurrentState()

            it.poolSizeValue.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar?, p1: Int, p2: Boolean) {
                    val newSize = bar?.progress ?: 0
                    preferenceApplier.poolSize = newSize + 1
                    it.poolSizeText.setText((newSize + 1).toString())
                }

                override fun onStartTrackingTouch(p0: SeekBar?) = Unit

                override fun onStopTrackingTouch(p0: SeekBar?) = Unit

            })
            it.poolSizeValue.progress = preferenceApplier.poolSize - 1

            it.valueBackgroundAlpha.setOnSeekBarChangeListener(
                    object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar?, p1: Int, p2: Boolean) {
                    val newSize = bar?.progress ?: 0
                    preferenceApplier.setWebViewBackgroundAlpha(newSize.toFloat() / 100f)
                    it.textBackgroundAlpha.setText(newSize.toString())
                }

                override fun onStartTrackingTouch(p0: SeekBar?) = Unit

                override fun onStopTrackingTouch(p0: SeekBar?) = Unit

            })
            it.valueBackgroundAlpha.progress =
                    (preferenceApplier.getWebViewBackgroundAlpha() * 100f).toInt()
        }

        binding.browserExpand.screenMode?.let {
            it.check(preferenceApplier.browserScreenMode().id())
            it.jumpDrawablesToCurrentState()
        }

        binding.menuPosRadio.let {
            it.check(preferenceApplier.menuPosId())
            it.jumpDrawablesToCurrentState()
        }
    }

    /**
     * Initialize expansion setting.
     */
    private fun initBrowserExpandable() {
        binding.browserExpand.screenMode?.setOnCheckedChangeListener { group, _ ->
            when (group.checkedRadioButtonId) {
                R.id.full_screen  -> preferenceApplier.setBrowserScreenMode(ScreenMode.FULL_SCREEN)
                R.id.expandable   -> preferenceApplier.setBrowserScreenMode(ScreenMode.EXPANDABLE)
                R.id.fixed        -> preferenceApplier.setBrowserScreenMode(ScreenMode.FIXED)
            }
        }
    }

    /**
     * Initialize menu position setting.
     */
    private fun initMenuPos() {
        binding.menuPosRadio.let {
            it.setOnCheckedChangeListener { group, _ ->
                when (group.checkedRadioButtonId) {
                    R.id.menu_pos_left  -> preferenceApplier.setMenuPos(MenuPos.LEFT)
                    R.id.menu_pos_right -> preferenceApplier.setMenuPos(MenuPos.RIGHT)
                }
            }
        }
    }

    /**
     * Switch retaining tabs.
     */
    fun switchRetainTabs() {
        val newState = !preferenceApplier.doesRetainTabs()
        preferenceApplier.setRetainTabs(newState)
        binding.retainTabsCheck.isChecked = newState
        @StringRes val messageId: Int = if (newState)
            R.string.message_check_retain_tabs
        else
            R.string.message_check_doesnot_retain_tabs
        Toaster.snackShort(binding.root, messageId, preferenceApplier.colorPair())
    }

    /***
     * Commit input.
     */
    fun commitHomeInput() {
        val input = binding.homeInputLayout.editText?.text.toString()
        if (TextUtils.isEmpty(input)) {
            Toaster.snackShort(
                    binding.root,
                    R.string.favorite_search_addition_dialog_empty_message,
                    preferenceApplier.colorPair()
            )
            return
        }
        if (Urls.isInvalidUrl(input)) {
            Toaster.snackShort(binding.root, R.string.message_invalid_url, preferenceApplier.colorPair())
            return
        }
        preferenceApplier.homeUrl = input

        Toaster.snackShort(
                binding.root,
                getString(R.string.message_commit_home, input),
                preferenceApplier.colorPair()
        )
    }

    /**
     * Switch content inversion enabling.
     */
    fun switchUseInversion() {
        val preferenceApplier = preferenceApplier
        val newState = !preferenceApplier.useInversion
        preferenceApplier.useInversion = newState
        binding.useInversionCheck.isChecked = newState
    }
    
    /**
     * Switch JavaScript enabling.
     */
    fun switchJsEnabled() {
        val preferenceApplier = preferenceApplier
        val newState = !preferenceApplier.useJavaScript()
        preferenceApplier.setUseJavaScript(newState)
        binding.browserJsCheck.isChecked = newState
        @StringRes val messageId: Int = if (newState)
            R.string.message_js_enabled
        else
            R.string.message_js_disabled
        Toaster.snackShort(binding.root, messageId, preferenceApplier.colorPair())
    }


    /**
     * Switch loading images.
     */
    fun switchLoadingImage() {
        val newState = !preferenceApplier.doesLoadImage()
        preferenceApplier.setLoadImage(newState)
        binding.useImageCheck.isChecked = newState
    }

    /**
     * Switching saving form data.
     */
    fun switchSaveFormData() {
        val newState = !preferenceApplier.doesSaveForm()
        preferenceApplier.setSaveForm(newState)
        binding.saveFormCheck.isChecked = newState
    }

    /**
     * Switch saving view history.
     */
    fun switchViewHistory() {
        val newState = !preferenceApplier.saveViewHistory
        preferenceApplier.saveViewHistory = newState
        binding.saveViewHistoryCheck.isChecked = newState
    }

    /**
     * Switch AD remover setting.
     */
    fun switchAdRemove() {
        val newState = !preferenceApplier.adRemove
        preferenceApplier.adRemove = newState
        binding.adRemoveCheck.isChecked = newState
    }

    /**
     * Clear [WebView] cache.
     *
     * @param snackbarParent for data binding
     */
    fun clearCache(snackbarParent: View) {
        WebView(context).clearCache(true)
        Toaster.snackShort(snackbarParent, R.string.done_clear, preferenceApplier.colorPair())
    }

    /**
     * Clear [WebView] form data.
     *
     * @param snackbarParent for data binding
     */
    fun clearFormData(snackbarParent: View) {
        WebView(context).clearFormData()
        Toaster.snackShort(snackbarParent, R.string.done_clear, preferenceApplier.colorPair())
    }

    /**
     * Clear all cookie.
     *
     * @param snackbarParent for data binding
     */
    fun clearCookie(snackbarParent: View) {
        CookieCleanerCompat(snackbarParent)
    }

    /**
     * UserAgent setting.
     */
    fun userAgent() {
        val fragmentManager = fragmentManager ?: return

        val dialogFragment = UserAgentDialogFragment()
        dialogFragment.setTargetFragment(this, 1)
        dialogFragment.show(
                fragmentManager,
                UserAgentDialogFragment::class.java.simpleName
        )
    }

    override fun onClickUserAgent(userAgent: UserAgent) {
        binding.userAgentValue.text = userAgent.title()
    }

    @StringRes
    override fun titleId() = R.string.subhead_setting_browser

    companion object {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.fragment_setting_browser

    }
}