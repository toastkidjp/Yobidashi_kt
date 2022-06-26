/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.input

import android.content.Context
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

/**
 * @author toastkidjp
 */
object Inputs {

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