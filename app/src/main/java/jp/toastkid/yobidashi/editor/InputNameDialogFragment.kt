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
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Inputs

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

        val input = EditText(activityContext).also {
            it.maxLines   = 1
            it.inputType  = InputType.TYPE_CLASS_TEXT
            it.imeOptions = EditorInfo.IME_ACTION_GO
            it.setText(DEFAULT_FILE_NAME)
            it.setSelection(DEFAULT_FILE_NAME.length)
        }

        val dialog = AlertDialog.Builder(activityContext)
                .setTitle(activityContext.getString(R.string.title_dialog_input_file_name))
                .setView(input)
                .setPositiveButton(R.string.save) { d, _ ->
                    saveAndClose(input, d)
                }
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .create()

        dialog.setOnShowListener {
            (activity as? Activity)?.let { activity ->
                input.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_GO) {
                        saveAndClose(input, dialog)
                        return@setOnEditorActionListener true
                    }
                    return@setOnEditorActionListener false
                }
                input.requestFocus()
                Inputs.showKeyboard(activity, input)
            }
        }

        return dialog
    }

    private fun saveAndClose(editText: EditText?, d: DialogInterface) {
        if (editText?.text?.isEmpty() == true) {
            d.dismiss()
            return
        }

        callback?.onClickInputName("${editText?.text.toString()}.txt")
        d.dismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
         * @param target [Fragment]
         */
        fun show(target: Fragment) {
            val dialogFragment = InputNameDialogFragment()
            dialogFragment.setTargetFragment(target, 1)
            val fragmentManager = target.fragmentManager ?: return
            dialogFragment.show(
                    fragmentManager,
                    InputNameDialogFragment::class.java.canonicalName
            )
        }
    }
}