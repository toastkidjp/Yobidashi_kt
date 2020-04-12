/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.editor

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.textfield.TextInputLayout
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserFragment
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.TextInputs

/**
 * Dialog for input name dialog.
 *
 * @author toastkidjp
 */
class InputNameDialogFragment : DialogFragment() {

    /**
     * Callback interface.
     */
    interface Callback {

        /**
         * Action using input name.
         * @param fileName
         */
        fun onClickInputName(fileName: String)
    }

    /**
     * Callback.
     */
    private var callback: Callback? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        val targetFragment = targetFragment
        if (targetFragment is Callback) {
            callback = targetFragment
        }

        val inputLayout = TextInputs.withDefaultInput(activityContext, DEFAULT_FILE_NAME)

        val dialog = AlertDialog.Builder(activityContext)
                .setTitle(activityContext.getString(R.string.title_dialog_input_file_name))
                .setView(inputLayout)
                .setPositiveButton(R.string.save) { d, _ ->
                    saveAndClose(inputLayout, d)
                }
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .create()
        dialog.setOnShowListener {
            (activity as? Activity)?.let { activity ->
                inputLayout.editText?.let { editText ->
                    Inputs.showKeyboard(activity, editText)
                }
            }
        }

        inputLayout.editText?.let {
            it.imeOptions = EditorInfo.IME_ACTION_GO
            it.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    saveAndClose(inputLayout, dialog)
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }
        }
        return dialog
    }

    private fun saveAndClose(inputLayout: TextInputLayout, d: DialogInterface) {
        if (inputLayout.editText?.text?.isEmpty() as Boolean) {
            d.dismiss()
            return
        }

        callback?.onClickInputName("${inputLayout.editText?.text.toString()}.txt")
        d.dismiss()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Inputs.showKeyboardForInputDialog(dialog?.window)
    }

    companion object {

        /**
         * Default file name.
         */
        private const val DEFAULT_FILE_NAME: String = "memo"

        /**
         * Show dialog.
         *
         * @param context [Context]
         */
        fun show(context: Context) {
            val dialogFragment = InputNameDialogFragment()

            if (context is FragmentActivity) {
                val supportFragmentManager = context.supportFragmentManager
                val target = supportFragmentManager
                        .findFragmentByTag(BrowserFragment::class.java.canonicalName)
                dialogFragment.setTargetFragment(target, 1)
                dialogFragment.show(
                        supportFragmentManager,
                        InputNameDialogFragment::class.java.canonicalName
                )
            }
        }
    }
}