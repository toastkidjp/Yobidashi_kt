package jp.toastkid.yobidashi.search.favorite

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.annotation.LayoutRes
import android.support.design.widget.TextInputLayout
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Spinner

import java.text.MessageFormat

import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FavoriteSearchAdditionDialogContentBinding
import jp.toastkid.yobidashi.libs.Colors
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.TextInputs
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.search.SearchCategorySpinnerInitializer

/**
 * Show input dialog and call inserting action.
 *
 * @param parent For using extract background color.
 * @author toastkidjp
 */
class Addition internal constructor(
        private val parent: ViewGroup,
        private val toasterCallback: (String) -> Unit
) {

    /** Context.  */
    private val context: Context = parent.context

    private val binding: FavoriteSearchAdditionDialogContentBinding

    private val categorySelector: Spinner

    private val input: EditText

    init {

        binding = DataBindingUtil.inflate<FavoriteSearchAdditionDialogContentBinding>(
                LayoutInflater.from(context),
                LAYOUT_ID,
                parent,
                false
        )
        binding.action = this
        val content = binding.root

        categorySelector = initSpinner(content)

        input = initInput(content)

        if (parent.childCount == 0) {
            parent.addView(content)
        }
    }

    /**
     * Show input dialog.
     */
    internal operator fun invoke() {
        val colorPair = PreferenceApplier(context).colorPair()
        Colors.setBgAndText(binding.close, colorPair)
        Colors.setBgAndText(binding.add, colorPair)
        parent.visibility = View.VISIBLE
    }

    /**
     * Initialize spinner.
     * @param content
     * *
     * @return
     */
    private fun initSpinner(content: View): Spinner {
        val categorySelector = content.findViewById(R.id.favorite_search_addition_categories) as Spinner
        SearchCategorySpinnerInitializer.initialize(categorySelector)
        return categorySelector
    }

    /**
     * Initialize input field.
     * @param content
     * *
     * @return
     */
    private fun initInput(content: View): EditText {
        val inputLayout = content.findViewById(R.id.favorite_search_addition_query) as TextInputLayout

        return TextInputs.setEmptyAlert(inputLayout)
    }

    fun cancel(v: View) {
        parent.visibility = View.GONE
        Inputs.hideKeyboard(input)
    }

    fun ok(v: View) {
        val query = input.text.toString()

        if (TextUtils.isEmpty(query)) {
            toasterCallback(context.getString(R.string.favorite_search_addition_dialog_empty_message))
            return
        }

        val category = categorySelector.selectedItem.toString()

        FavoriteSearchInsertion(context, category, query).insert()

        val message = MessageFormat.format(
                context.getString(R.string.favorite_search_addition_successful_format),
                query
        )
        toasterCallback(message)
    }

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.favorite_search_addition_dialog_content
    }
}