package jp.toastkid.search.view.setting

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.SearchCategory

class SearchSettingViewModel {

    private val spinnerOpen = mutableStateOf(false)

    fun spinnerOpen() = spinnerOpen.value

    fun openSpinner() {
        spinnerOpen.value = true
    }

    fun closeSpinner() {
        spinnerOpen.value = false
    }

    private val enableSearchQueryExtractCheck =
        mutableStateOf(false)

    fun enableSearchQueryExtractCheck() = enableSearchQueryExtractCheck.value

    fun setEnableSearchQueryExtractCheck(newValue: Boolean) {
        enableSearchWithClipCheck.value = newValue
    }

    private val enableSearchWithClipCheck =
        mutableStateOf(false)

    fun enableSearchWithClipCheck() = enableSearchWithClipCheck.value

    fun setEnableSearchWithClipCheck(newValue: Boolean) {
        enableSearchWithClipCheck.value = newValue
    }

    private val useSuggestionCheck = mutableStateOf(false)

    fun useSuggestionCheck() = useSuggestionCheck.value

    fun setUseSuggestionCheck(newValue: Boolean) {
        useSuggestionCheck.value = newValue
    }

    private val useHistoryCheck = mutableStateOf(false)

    fun useHistoryCheck() = useHistoryCheck.value

    fun setUseHistoryCheck(newValue: Boolean) {
        useHistoryCheck.value = newValue
    }

    private val useFavoriteCheck = mutableStateOf(false)

    fun useFavoriteCheck() = useFavoriteCheck.value

    fun setUseFavoriteCheck(newValue: Boolean) {
        useFavoriteCheck.value = newValue
    }

    private val useViewHistoryCheck = mutableStateOf(false)

    fun useViewHistoryCheck() = useViewHistoryCheck.value

    fun setUseViewHistoryCheck(newValue: Boolean) {
        useViewHistoryCheck.value = newValue
    }

    private val useUrlModuleCheck = mutableStateOf(false)

    fun useUrlModuleCheck() = useUrlModuleCheck.value

    fun setUseUrlModuleCheck(newValue: Boolean) {
        useUrlModuleCheck.value = newValue
    }

    private val useTrendCheck = mutableStateOf(false)

    fun useTrendCheck() = useTrendCheck.value

    fun setUseTrendCheck(newValue: Boolean) {
        useTrendCheck.value = newValue
    }

    private val categoryName =
        mutableStateOf(
            SearchCategory.getDefaultCategoryName()
        )

    fun categoryName() = categoryName.value

    fun setCategoryName(newValue: String) {
        categoryName.value = newValue
    }

    private val selections = mutableStateSetOf<SearchCategory>()

    fun selections() = selections

    fun onSelectionChange(selection: SearchCategory) {
        if (selections.contains(selection)) {
            selections.remove(selection)
            return
        }
        selections.add(selection)
    }

    fun onSwitchSelectAll(newState: Boolean) {
        if (newState) {
            selections.addAll(SearchCategory.entries)
            return
        }
        selections.clear()
    }

    fun launch(preferenceApplier: PreferenceApplier) {
        enableSearchQueryExtractCheck.value = preferenceApplier.enableSearchQueryExtract
        enableSearchWithClipCheck.value = preferenceApplier.enableSearchWithClip
        useSuggestionCheck.value = preferenceApplier.isEnableSuggestion
        useHistoryCheck.value = preferenceApplier.isEnableSearchHistory
        useFavoriteCheck.value = preferenceApplier.isEnableFavoriteSearch
        useViewHistoryCheck.value = preferenceApplier.isEnableViewHistory
        useUrlModuleCheck.value = preferenceApplier.isEnableUrlModule()
        useTrendCheck.value = preferenceApplier.isEnableTrendModule()

        val defaultSearchEngine = preferenceApplier.getDefaultSearchEngine()
        if (defaultSearchEngine.isNullOrBlank().not()) {
            categoryName.value = defaultSearchEngine
        }

        selections.clear()
        val disableSearchCategory = preferenceApplier.readDisableSearchCategory() ?: emptySet()
        SearchCategory.entries
            .filterNot { disableSearchCategory.contains(it.name) }
            .forEach(selections::add)
    }

}