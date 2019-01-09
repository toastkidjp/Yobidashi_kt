/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.bookmark

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.TextInputs

/**
 * @author toastkidjp
 */
class AddingFolderDialogFragment : DialogFragment() {

    interface OnClickAddingFolder {
        fun onClickAddFolder(title: String?)
    }

    private var onClick: OnClickAddingFolder? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        if (activityContext is OnClickAddingFolder) {
            onClick = activityContext
        }

        val inputLayout = TextInputs.make(activityContext)

        return AlertDialog.Builder(activityContext)
                .setTitle(getString(R.string.title_dialog_input_file_name))
                .setView(inputLayout)
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .setPositiveButton(R.string.ok) { d, _ ->
                    onClick?.onClickAddFolder(inputLayout.editText?.text?.toString())
                    d.dismiss()
                }
                .setCancelable(true)
                .create()
    }
}