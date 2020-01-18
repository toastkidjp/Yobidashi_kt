/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.cleaner

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
class CleanerResultDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = context
                ?: return super.onCreateDialog(savedInstanceState)

        val message = arguments?.getString(KEY_MESSAGE)
                ?: return super.onCreateDialog(savedInstanceState)

        return AlertDialog.Builder(context)
                .setTitle("Cleaned")
                .setMessage(message)
                .setPositiveButton(R.string.close) { d, _ ->
                    d.dismiss()
                }
                .create()
    }

    companion object {

        private const val KEY_MESSAGE = "message"

        fun withMessage(message: String) =
                CleanerResultDialogFragment().also {
                    it.arguments = bundleOf(KEY_MESSAGE to message)
                }
    }
}