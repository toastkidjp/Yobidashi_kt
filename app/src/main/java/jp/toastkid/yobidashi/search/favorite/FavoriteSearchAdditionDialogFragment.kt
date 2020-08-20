package jp.toastkid.yobidashi.search.favorite

import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.SearchCategory
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.DialogFavoriteSearchAdditionBinding
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.TextInputs
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.search.category.SearchCategoryAdapter
import java.text.MessageFormat

/**
 * Show input dialog and call inserting action.
 *
 * @author toastkidjp
 */
class FavoriteSearchAdditionDialogFragment: BottomSheetDialogFragment() {

    /**
     * Binding object.
     */
    private lateinit var binding: DialogFavoriteSearchAdditionBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                LAYOUT_ID,
                container,
                false
        )

        binding.action = this

        initSpinner()

        initInput()

        return binding.root
    }

    /**
     * Initialize spinner.
     */
    private fun initSpinner() {
        val context = context ?: return
        binding.favoriteSearchAdditionCategories.adapter = SearchCategoryAdapter(context)
        val index = SearchCategory.findIndex(
                PreferenceApplier(context).getDefaultSearchEngine() ?: SearchCategory.getDefaultCategoryName()
        )
        binding.favoriteSearchAdditionCategories.setSelection(index)
    }

    /**
     * Initialize input field.
     */
    private fun initInput() {
        TextInputs.setEmptyAlert(binding.favoriteSearchAdditionQuery)
        binding.favoriteSearchAdditionQuery.editText?.setOnEditorActionListener { _, _, _ ->
            ok()
            return@setOnEditorActionListener true
        }
    }

    override fun onResume() {
        super.onResume()

        PreferenceApplier(binding.root.context).colorPair().setTo(binding.add)
    }

    override fun onDismiss(dialog: DialogInterface) {
        Inputs.hideKeyboard(binding.favoriteSearchAdditionQueryInput)
        reload()
        super.onDismiss(dialog)
    }

    override fun onCancel(dialog: DialogInterface) {
        reload()
        super.onCancel(dialog)
    }

    private fun reload() {
        val target = targetFragment ?: return
        ViewModelProvider(target)
                .get(FavoriteSearchFragmentViewModel::class.java)
                .reload()
    }

    /**
     * Ok action.
     */
    fun ok() {
        val query = binding.favoriteSearchAdditionQueryInput.text?.toString()

        if (TextUtils.isEmpty(query)) {
            Toaster.snackShort(
                    binding.root,
                    getString(R.string.favorite_search_addition_dialog_empty_message),
                    PreferenceApplier(binding.root.context).colorPair()
            )
            return
        }

        val category = binding.favoriteSearchAdditionCategories.selectedItem.toString()

        FavoriteSearchInsertion(binding.root.context, category, query).invoke()

        val message = MessageFormat.format(
                getString(R.string.favorite_search_addition_successful_format),
                query
        )
        Toaster.snackShort(
                binding.root,
                message,
                PreferenceApplier(binding.root.context).colorPair()
        )
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID = R.layout.dialog_favorite_search_addition
    }
}