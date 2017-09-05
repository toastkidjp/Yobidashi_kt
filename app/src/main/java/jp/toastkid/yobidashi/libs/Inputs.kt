package jp.toastkid.yobidashi.libs

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * @author toastkidjp
 */
object Inputs {

    /**
     * Show software keyboard.
     * @param activity
     *
     * @param editText
     */
    fun showKeyboard(activity: Activity, editText: EditText) {
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (!inputMethodManager.isActive) {
            return
        }
        inputMethodManager.showSoftInput(editText, 0)
    }

    /**
     * For Fragment.
     * @param activity
     */
    fun toggle(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(
                InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    /**
     * Hide software keyboard.
     * @param v
     */
    fun hideKeyboard(v: View) {
        val manager = v.context
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (!manager.isActive) {
            return
        }
        manager.hideSoftInputFromWindow(
                v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}
