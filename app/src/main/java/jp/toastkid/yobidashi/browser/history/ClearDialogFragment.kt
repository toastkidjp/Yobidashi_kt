/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.history

import android.app.Dialog
import android.os.Bundle
import android.text.Html
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
class ClearDialogFragment : DialogFragment() {

    interface Callback {
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        return AlertDialog.Builder(activityContext)
                .setTitle(R.string.title_clear_view_history)
                .setMessage(
                    Html.fromHtml(
                        getString(R.string.confirm_clear_all_settings),
                        Html.FROM_HTML_MODE_COMPACT
                    )
                )
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .setPositiveButton(R.string.ok) { d, _ ->
                    parentFragmentManager.setFragmentResult("clear_items", Bundle.EMPTY)
                    d.dismiss()
                }
                .create()
    }
}
