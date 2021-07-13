/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.tab.tab_list

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
class TabListClearDialogFragment : DialogFragment() {

    interface Callback {
        fun onClickClear()
    }

    private var onClick: Callback? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        if (activityContext is Callback) {
            onClick = activityContext
        }

        return AlertDialog.Builder(activityContext)
                .setTitle(getString(R.string.title_clear_all_tabs))
                .setMessage(
                    HtmlCompat.fromHtml(
                        getString(R.string.confirm_clear_all_settings),
                        HtmlCompat.FROM_HTML_MODE_COMPACT
                    )
                )
                .setCancelable(true)
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .setPositiveButton(R.string.ok) { d, _ ->
                    onClick?.onClickClear()
                    d.dismiss()
                }
                .create()
    }

}