/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.main

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
class CloseDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)
        return AlertDialog.Builder(activityContext)
                .setTitle(R.string.confirmation)
                .setMessage(R.string.message_confirm_exit)
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .setPositiveButton(R.string.ok) { d, _ ->
                    d.dismiss()
                    if (activityContext is Activity) {
                        activityContext.finish()
                    }
                }
                .create()
    }
}