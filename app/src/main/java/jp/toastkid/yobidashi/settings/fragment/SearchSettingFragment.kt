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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
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

    private lateinit var binding: FragmentSettingSearchBinding

    private lateinit var preferenceApplier: PreferenceApplier

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setting_search, container, false)
        val activityContext = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(activityContext)
        binding.fragment = this

        SearchCategorySpinnerInitializer.invoke(binding.searchCategories as Spinner)
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
        binding.useAppSearchCheck.isChecked = preferenceApplier.isEnableAppSearch()
        binding.useAppSearchCheck.jumpDrawablesToCurrentState()
    }

    /**
     * Open search categories spinner.
     *
     * @param v
     */
    fun openSearchCategory(v: View) {
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
     * @param v
     */
    fun switchSearchWithClip(v: View) {
        val newState = !preferenceApplier.enableSearchWithClip
        preferenceApplier.enableSearchWithClip = newState
        binding.enableSearchWithClipCheck.isChecked = newState

        @StringRes val messageId: Int
                = if (newState) { R.string.message_enable_swc } else { R.string.message_disable_swc }
        Toaster.snackShort(binding.root, messageId, preferenceApplier.colorPair())
    }

    fun switchUseSuggestion(v: View) {
        val newState = !preferenceApplier.isEnableSuggestion
        preferenceApplier.switchEnableSuggestion()
        binding.useSuggestionCheck.isChecked = newState
    }

    fun switchUseSearchHistory(v: View) {
        val newState = !preferenceApplier.isEnableSearchHistory
        preferenceApplier.switchEnableSearchHistory()
        binding.useHistoryCheck.isChecked = newState
    }

    fun switchUseFavoriteSearch(v: View) {
        val newState = !preferenceApplier.isEnableFavoriteSearch
        preferenceApplier.switchEnableFavoriteSearch()
        binding.useFavoriteCheck.isChecked = newState
    }

    fun switchUseViewHistory(v: View) {
        val newState = !preferenceApplier.isEnableViewHistory
        preferenceApplier.switchEnableViewHistory()
        binding.useViewHistoryCheck.isChecked = newState
    }

    fun switchUseAppSearch() {
        val newState = !preferenceApplier.isEnableAppSearch()
        preferenceApplier.switchEnableAppSearch()
        binding.useAppSearchCheck.isChecked = newState
    }

    override fun titleId() = R.string.subhead_search

}