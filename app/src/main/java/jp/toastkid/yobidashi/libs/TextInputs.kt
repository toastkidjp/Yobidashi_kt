package jp.toastkid.yobidashi.libs

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
class TextInputs private constructor() {

    init {
        throw IllegalStateException()
    }

    companion object {

        fun setEmptyAlert(inputLayout: TextInputLayout): EditText {
            val input = inputLayout.editText
            if (input == null) {
                return input
            }

            input.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable) {
                    if (s.toString().length == 0) {
                        inputLayout.error = inputLayout.context
                                .getString(R.string.favorite_search_addition_dialog_empty_message)
                        return
                    }
                    inputLayout.isErrorEnabled = false
                }
            })
            return input
        }

        fun make(context: Context): TextInputLayout {
            val inputLayout = TextInputLayout(context)
            inputLayout.addView(EditText(context))
            return inputLayout
        }
    }
}
