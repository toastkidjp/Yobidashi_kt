package jp.toastkid.yobidashi.search.favorite

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.text.EmptyAlertSetter
import jp.toastkid.search.SearchCategory
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.DialogFavoriteSearchAdditionBinding
import jp.toastkid.lib.input.Inputs
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
    ): View {
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
        EmptyAlertSetter().invoke(binding.favoriteSearchAdditionQuery)
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
        (arguments?.get(KEY_VIEW_MODEL) as? FavoriteSearchFragmentViewModel)?.reload()
    }

    /**
     * Ok action.
     */
    fun ok() {
        val query = binding.favoriteSearchAdditionQueryInput.text?.toString()

        if (query.isNullOrEmpty()) {
            Toaster.snackShort(
                    binding.root,
                    getString(R.string.favorite_search_addition_dialog_empty_message),
                    PreferenceApplier(binding.root.context).colorPair()
            )
            return
        }

        val category = binding.favoriteSearchAdditionCategories.selectedItem.toString()

        FavoriteSearchInsertion(binding.root.context, category, query).invoke()

        reload()

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

        private const val KEY_VIEW_MODEL = "view_model"

        fun show(parentFragment: Fragment) {
            val fragment = FavoriteSearchAdditionDialogFragment()
            fragment.arguments = bundleOf(
                KEY_VIEW_MODEL to
                        ViewModelProvider(parentFragment).get(FavoriteSearchFragmentViewModel::class.java)
            )
            fragment.show(
                parentFragment.parentFragmentManager,
                fragment::class.java.canonicalName
            )
        }

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID = R.layout.dialog_favorite_search_addition
    }
}