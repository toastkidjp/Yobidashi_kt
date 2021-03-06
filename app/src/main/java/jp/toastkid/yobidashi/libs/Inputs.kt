package jp.toastkid.yobidashi.libs

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * @author toastkidjp
 */
object Inputs {

    /**
     * Show software keyboard.
     *
     * @param activity
     * @param editText
     */
    fun showKeyboard(activity: Activity, editText: EditText) {
        val inputMethodManager = obtainInputManager(activity)
        inputMethodManager?.showSoftInput(editText, 0)
    }

    /**
     * Hide software keyboard.
     *
     * @param v
     */
    fun hideKeyboard(v: View?) {
        val manager = obtainInputManager(v?.context)
        manager?.hideSoftInputFromWindow(v?.windowToken, 0)
    }

    private fun obtainInputManager(context: Context?): InputMethodManager? {
        val inputMethodManager =
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                ?: return null
        if (!inputMethodManager.isActive) {
            return null
        }
        return inputMethodManager
    }

    /**
     * Show software keyboard for input dialog.
     * You should call this method from `onActivityCreated(savedInstanceState: Bundle?)`.
     *
     * @param window Nullable [Window] for calling setSoftInputMode.
     */
    fun showKeyboardForInputDialog(window: Window?) {
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }
}
