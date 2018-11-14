/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.editor

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserFragment

/**
 * @author toastkidjp
 */
class PasteAsConfirmationDialog : DialogFragment() {

    interface Callback {
        fun onClickPasteAs()
    }

    private var callback: Callback? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        val targetFragment = targetFragment
        if (targetFragment is Callback) {
            callback = targetFragment
        }
        return AlertDialog.Builder(activityContext)
                .setTitle(activityContext.getString(R.string.title_dialog_input_file_name))
                .setMessage(R.string.message_paste_as_dialog)
                .setPositiveButton(R.string.save) { d, _ ->
                    callback?.onClickPasteAs()
                    d.dismiss()
                }
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .create()
    }

    companion object {
        fun show(context: Context) {
            val dialogFragment = PasteAsConfirmationDialog()

            if (context is FragmentActivity) {
                val supportFragmentManager = context.supportFragmentManager
                val target = supportFragmentManager
                        .findFragmentByTag(BrowserFragment::class.java.simpleName)
                dialogFragment.setTargetFragment(target, 1)
                dialogFragment.show(
                        supportFragmentManager,
                        dialogFragment::class.java.simpleName
                )
            }
        }
    }
}