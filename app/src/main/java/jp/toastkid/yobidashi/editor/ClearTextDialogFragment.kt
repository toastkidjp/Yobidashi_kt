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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.HtmlCompat

/**
 * Clear text confirmation dialog.
 *
 * @author toastkidjp
 */
class ClearTextDialogFragment : DialogFragment() {

    interface Callback {
        fun onClickClearInput()
    }

    /**
     * Callback of clicked positive button.
     */
    private var callback: Callback? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        val targetFragment = targetFragment
        if (targetFragment is Callback) {
            callback = targetFragment
        }

        return AlertDialog.Builder(activityContext)
                .setTitle(activityContext.getString(R.string.title_clear_text))
                .setMessage(HtmlCompat.fromHtml(activityContext.getString(R.string.confirm_clear_all_settings)))
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .setPositiveButton(R.string.ok) { d, _ ->
                    callback?.onClickClearInput()
                    d.dismiss()
                }
                .create()
    }

    companion object {

        /**
         * Show this dialog.
         *
         * @param target [androidx.fragment.app.Fragment]
         */
        fun show(target: Fragment) {
            val dialogFragment = ClearTextDialogFragment()
            dialogFragment.setTargetFragment(target, 1)
            dialogFragment.show(
                    target.parentFragmentManager,
                    dialogFragment::class.java.canonicalName
            )
        }
    }
}