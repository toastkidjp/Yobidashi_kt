package jp.toastkid.jitte.search.favorite

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
import io.reactivex.functions.Consumer

import java.text.MessageFormat

import jp.toastkid.jitte.R
import jp.toastkid.jitte.databinding.FavoriteSearchAdditionDialogContentBinding
import jp.toastkid.jitte.libs.Colors
import jp.toastkid.jitte.libs.Inputs
import jp.toastkid.jitte.libs.TextInputs
import jp.toastkid.jitte.libs.preference.PreferenceApplier
import jp.toastkid.jitte.search.SearchCategorySpinnerInitializer

/**
 * Show input dialog and call inserting action.

 * @author toastkidjp
 */
class Addition
/**

 * @param view
 */
internal constructor(
        /** For using extract background color.  */
        private val parent: ViewGroup,
        private val toasterCallback: Consumer<String>
) {

    /** Context.  */
    private val context: Context

    private val binding: FavoriteSearchAdditionDialogContentBinding

    private val categorySelector: Spinner

    private val input: EditText

    init {
        this.context = parent.context

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
            toasterCallback.accept(context.getString(R.string.favorite_search_addition_dialog_empty_message))
            return
        }

        val category = categorySelector.selectedItem.toString()

        FavoriteSearchInsertion(context, category, query).insert()

        val message = MessageFormat.format(
                context.getString(R.string.favorite_search_addition_successful_format),
                query
        )
        toasterCallback.accept(message)
    }

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.favorite_search_addition_dialog_content
    }
}