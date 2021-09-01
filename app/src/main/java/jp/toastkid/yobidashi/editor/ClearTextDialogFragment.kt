/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.editor

import android.app.Dialog
import android.os.Bundle
import android.text.Html
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import jp.toastkid.yobidashi.R

/**
 * Clear text confirmation dialog.
 *
 * @author toastkidjp
 */
class ClearTextDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        return AlertDialog.Builder(activityContext)
                .setTitle(activityContext.getString(R.string.title_clear_text))
                .setMessage(
                    Html.fromHtml(
                        activityContext.getString(R.string.confirm_clear_all_settings),
                        Html.FROM_HTML_MODE_COMPACT
                    )
                )
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .setPositiveButton(R.string.ok) { d, _ ->
                    parentFragmentManager.setFragmentResult("clear_input", Bundle.EMPTY)
                    d.dismiss()
                }
                .create()
    }

    companion object {

        /**
         * Show this dialog.
         *
         * @param fragmentManager Use for showing dialog
         */
        fun show(fragmentManager: FragmentManager) {
            val dialogFragment = ClearTextDialogFragment()
            dialogFragment.show(
                fragmentManager,
                dialogFragment::class.java.canonicalName
            )
        }
    }
}