package jp.toastkid.yobidashi.libs

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import jp.toastkid.yobidashi.R

/**
 * [TextInputLayout] utility.
 *
 * @author toastkidjp
 */
object TextInputs  {

    /**
     * EditText's layout params.
     */
    private val LAYOUT_PARAMS = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
    )

    /**
     * Set empty alert.
     *
     * @param inputLayout [TextInputLayout]
     */
    fun setEmptyAlert(inputLayout: TextInputLayout): EditText {
        val input: EditText? = inputLayout.editText

        input?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable) {
                if (s.toString().isEmpty()) {
                    inputLayout.error = inputLayout.context
                            .getString(R.string.favorite_search_addition_dialog_empty_message)
                    return
                }
                inputLayout.isErrorEnabled = false
            }
        })
        return input ?: EditText(inputLayout.context)
    }

    /**
     * Make [TextInputLayout] instance.
     *
     * @param context [Context] Use for make instance.
     */
    fun make(context: Context): TextInputLayout =
            TextInputLayout(context)
                .apply {
                    addView(
                            EditText(context).apply {
                                maxLines   = 1
                                inputType  = InputType.TYPE_CLASS_TEXT
                                imeOptions = EditorInfo.IME_ACTION_SEARCH
                            },
                            0,
                            LAYOUT_PARAMS
                    )
                }

    /**
     * Make [TextInputLayout] instance with default input text.
     *
     * @param context [Context] Use for make instance.
     * @param defaultInput Default input text
     */
    fun withDefaultInput(context: Context, defaultInput: CharSequence): TextInputLayout {
        val inputLayout = make(context)
        inputLayout.editText?.also {
            it.setText(defaultInput)
            it.setSelection(defaultInput.length)
        }
        return inputLayout
    }
}
