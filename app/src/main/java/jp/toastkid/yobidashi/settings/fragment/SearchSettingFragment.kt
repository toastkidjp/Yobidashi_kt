/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentSettingSearchBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.search.SearchCategory
import jp.toastkid.yobidashi.search.SearchCategorySpinnerInitializer

/**
 * @author toastkidjp
 */
class SearchSettingFragment : Fragment(), TitleIdSupplier {

    /**
     * View Data Binding object.
     */
    private lateinit var binding: FragmentSettingSearchBinding

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        val activityContext = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(activityContext)
        binding.fragment = this

        SearchCategorySpinnerInitializer.invoke(binding.searchCategories)
        binding.searchCategories.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                preferenceApplier.setDefaultSearchEngine(
                        SearchCategory.values()[binding.searchCategories.selectedItemPosition].name)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        binding.enableSearchQueryExtractCheck.isChecked = preferenceApplier.enableSearchQueryExtract
        binding.enableSearchQueryExtractCheck.jumpDrawablesToCurrentState()
        binding.enableSearchWithClipCheck.isChecked = preferenceApplier.enableSearchWithClip
        binding.enableSearchWithClipCheck.jumpDrawablesToCurrentState()
        binding.useSuggestionCheck.isChecked = preferenceApplier.isEnableSuggestion
        binding.useSuggestionCheck.jumpDrawablesToCurrentState()
        binding.useHistoryCheck.isChecked = preferenceApplier.isEnableSearchHistory
        binding.useHistoryCheck.jumpDrawablesToCurrentState()
        binding.useFavoriteCheck.isChecked = preferenceApplier.isEnableFavoriteSearch
        binding.useFavoriteCheck.jumpDrawablesToCurrentState()
        binding.useViewHistoryCheck.isChecked = preferenceApplier.isEnableViewHistory
        binding.useViewHistoryCheck.jumpDrawablesToCurrentState()
        binding.useUrlModuleCheck.isChecked = preferenceApplier.isEnableUrlModule()
        binding.useUrlModuleCheck.jumpDrawablesToCurrentState()
        binding.useAppSearchCheck.isChecked = preferenceApplier.isEnableAppSearch()
        binding.useAppSearchCheck.jumpDrawablesToCurrentState()
    }

    /**
     * Open search categories spinner.
     */
    fun openSearchCategory() {
        binding.searchCategories.performClick()
    }

    /**
     * Switch search query extraction.
     */
    fun switchSearchQueryExtract() {
        val newState = !preferenceApplier.enableSearchQueryExtract
        preferenceApplier.enableSearchQueryExtract = newState
        binding.enableSearchQueryExtractCheck.isChecked = newState
    }

    /**
     * Switch notification widget displaying.
     *
     * @param v only use snackbar's parent.
     */
    fun switchSearchWithClip(v: View) {
        val newState = !preferenceApplier.enableSearchWithClip
        preferenceApplier.enableSearchWithClip = newState
        binding.enableSearchWithClipCheck.isChecked = newState

        @StringRes val messageId: Int
                = if (newState) { R.string.message_enable_swc } else { R.string.message_disable_swc }
        Toaster.snackShort(v, messageId, preferenceApplier.colorPair())
    }

    /**
     * Switch state of using query suggestion.
     */
    fun switchUseSuggestion() {
        val newState = !preferenceApplier.isEnableSuggestion
        preferenceApplier.switchEnableSuggestion()
        binding.useSuggestionCheck.isChecked = newState
    }

    /**
     * Switch state of using search history words suggestion.
     */
    fun switchUseSearchHistory() {
        val newState = !preferenceApplier.isEnableSearchHistory
        preferenceApplier.switchEnableSearchHistory()
        binding.useHistoryCheck.isChecked = newState
    }

    /**
     * Switch state of using favorite search word suggestion.
     */
    fun switchUseFavoriteSearch() {
        val newState = !preferenceApplier.isEnableFavoriteSearch
        preferenceApplier.switchEnableFavoriteSearch()
        binding.useFavoriteCheck.isChecked = newState
    }

    /**
     * Switch state of using view history suggestion.
     */
    fun switchUseViewHistory() {
        val newState = !preferenceApplier.isEnableViewHistory
        preferenceApplier.switchEnableViewHistory()
        binding.useViewHistoryCheck.isChecked = newState
    }

    /**
     * Switch state of using view history suggestion.
     */
    fun switchUseUrlModule() {
        val newState = !preferenceApplier.isEnableUrlModule()
        preferenceApplier.switchEnableUrlModule()
        binding.useUrlModuleCheck.isChecked = newState
    }

    /**
     * Switch state of using App search suggestion.
     */
    fun switchUseAppSearch() {
        val newState = !preferenceApplier.isEnableAppSearch()
        preferenceApplier.switchEnableAppSearch()
        binding.useAppSearchCheck.isChecked = newState
    }

    @StringRes
    override fun titleId() = R.string.subhead_search

    companion object {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.fragment_setting_search
    }
}