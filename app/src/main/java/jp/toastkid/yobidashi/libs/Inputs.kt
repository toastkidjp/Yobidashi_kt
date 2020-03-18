package jp.toastkid.yobidashi.libs

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import timber.log.Timber

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
    fun hideKeyboard(v: View?) {
        Timber.i("called")
        val manager = v?.context
                ?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return
        if (!manager.isActive) {
            Timber.i("inactive")
            return
        }
        Timber.i("hide")
        manager.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
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
