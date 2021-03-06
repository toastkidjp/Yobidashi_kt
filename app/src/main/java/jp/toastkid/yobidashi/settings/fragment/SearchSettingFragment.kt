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
import jp.toastkid.lib.color.IconColorFinder
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.CompoundDrawableColorApplier
import jp.toastkid.search.SearchCategory
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentSettingSearchBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.search.category.SearchCategoryAdapter
import jp.toastkid.yobidashi.settings.fragment.search.category.Adapter

/**
 * @author toastkidjp
 */
class SearchSettingFragment : Fragment() {

    /**
     * View Data Binding object.
     */
    private lateinit var binding: FragmentSettingSearchBinding

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        val activityContext = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(activityContext)
        binding.fragment = this

        binding.searchCategories.adapter = SearchCategoryAdapter(activityContext)
        val index = SearchCategory.findIndex(
            PreferenceApplier(activityContext).getDefaultSearchEngine()
                ?: SearchCategory.getDefaultCategoryName()
        )
        binding.searchCategories.setSelection(index)
        binding.searchCategories.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                preferenceApplier.setDefaultSearchEngine(
                        SearchCategory.values()[binding.searchCategories.selectedItemPosition].name)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        val adapter = Adapter(preferenceApplier)
        binding.settingSearchCategories.adapter = adapter
        binding.checkSearchCategory.setOnClickListener {
            adapter.invokeCheckAll()
        }
        adapter.notifyDataSetChanged()

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
        binding.useTrendCheck.isChecked = preferenceApplier.isEnableTrendModule()
        binding.useTrendCheck.jumpDrawablesToCurrentState()

        val color = IconColorFinder.from(binding.root).invoke()
        CompoundDrawableColorApplier().invoke(
                color,
                binding.textUseViewHistory,
                binding.textDefaultSearchEngine,
                binding.textEnableSearchQueryExtract,
                binding.textEnableSearchWithClip,
                binding.textUseFavorite,
                binding.textUseHistory,
                binding.textUseSuggestion,
                binding.textUseTrend,
                binding.textUseUrlModule,
                binding.textSearchCategory
        )
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
     * @param v only use [com.google.android.material.snackbar.Snackbar]'s parent.
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
     * Switch state of using trend suggestion.
     */
    fun switchUseTrendModule() {
        val newState = !preferenceApplier.isEnableTrendModule()
        preferenceApplier.switchEnableTrendModule()
        binding.useTrendCheck.isChecked = newState
    }

    companion object : TitleIdSupplier {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.fragment_setting_search

        @StringRes
        override fun titleId() = R.string.subhead_search

    }
}