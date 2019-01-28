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
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserFragment
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.TextInputs

/**
 * @author toastkidjp
 */
class InputNameDialogFragment : DialogFragment() {

    interface Callback {
        fun onClickInputName(fileName: String)
    }

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
                    if (inputLayout.editText?.text?.isEmpty() as Boolean) {
                        d.dismiss()
                        return@setPositiveButton
                    }

                    callback?.onClickInputName("${inputLayout.editText?.text.toString()}.txt")
                    d.dismiss()
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
        return dialog
    }

    companion object {

        /**
         * Default file name.
         */
        private const val DEFAULT_FILE_NAME: String = "memo"

        fun show(context: Context) {
            val dialogFragment = InputNameDialogFragment()

            if (context is FragmentActivity) {
                val supportFragmentManager = context.supportFragmentManager
                val target = supportFragmentManager
                        .findFragmentByTag(BrowserFragment::class.java.simpleName)
                dialogFragment.setTargetFragment(target, 1)
                dialogFragment.show(
                        supportFragmentManager,
                        InputNameDialogFragment::class.java.simpleName
                )
            }
        }
    }
}