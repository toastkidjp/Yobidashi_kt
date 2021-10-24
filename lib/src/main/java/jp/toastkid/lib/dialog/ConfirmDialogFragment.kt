/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import jp.toastkid.lib.R

class ConfirmDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        val title = arguments?.getString(KEY_TITLE) ?: return super.onCreateDialog(savedInstanceState)
        val message = arguments?.getCharSequence(KEY_MESSAGE) ?: return super.onCreateDialog(savedInstanceState)
        val key = arguments?.getString(KEY_KEY) ?: return super.onCreateDialog(savedInstanceState)

        return AlertDialog.Builder(activityContext)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(true)
            .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
            .setPositiveButton(R.string.ok) { d, _ ->
                parentFragmentManager.setFragmentResult(key, bundleOf(key to true))
                arguments?.clear()
                d.dismiss()
            }
            .create()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        arguments?.clear()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        arguments?.clear()
    }

    companion object {

        private const val KEY_TITLE = "title"

        private const val KEY_MESSAGE = "message"

        private const val KEY_KEY = "key"

        fun show(fragmentManager: FragmentManager, title: String, message: CharSequence, key: String) {
            val confirmDialogFragment = ConfirmDialogFragment()
                .also { confirmDialogFragment ->
                    confirmDialogFragment.arguments = bundleOf(
                        KEY_TITLE to title,
                        KEY_MESSAGE to message,
                        KEY_KEY to key
                    )
                }
            confirmDialogFragment.show(fragmentManager, key)
        }

    }

}