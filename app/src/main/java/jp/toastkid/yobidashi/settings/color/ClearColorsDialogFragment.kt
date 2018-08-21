/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.color

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.Html
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
class ClearColorsDialogFragment : DialogFragment() {

    interface Callback {
        fun onClickClearColor()
    }

    private var onClick: Callback? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        if (activityContext is Callback) {
            onClick = activityContext
        }

        return AlertDialog.Builder(activityContext)
                .setTitle(R.string.title_clear_saved_color)
                .setMessage(Html.fromHtml(activityContext.getString(R.string.confirm_clear_all_settings)))
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .setPositiveButton(R.string.ok) { d, _ ->
                    onClick?.onClickClearColor()
                    d.dismiss()
                }
                .create()
    }

}