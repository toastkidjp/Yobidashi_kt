package jp.toastkid.yobidashi.libs

import android.content.Context
import android.text.InputType
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import com.google.android.material.textfield.TextInputLayout

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
     * Make [TextInputLayout] instance.
     *
     * @param context [Context] Use for make instance.
     */
    fun make(context: Context): TextInputLayout =
            TextInputLayout(context)
                .also { layout ->
                    layout.addView(
                            EditText(context).also {
                                it.maxLines   = 1
                                it.inputType  = InputType.TYPE_CLASS_TEXT
                                it.imeOptions = EditorInfo.IME_ACTION_SEARCH
                            },
                            0,
                            LAYOUT_PARAMS
                    )
                }
}
