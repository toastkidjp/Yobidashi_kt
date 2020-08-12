/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.tls

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
class TlsErrorDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)
        return AlertDialog.Builder(activityContext)
                .setTitle(R.string.title_ssl_connection_error)
                .setMessage(arguments?.getString(KEY_MESSAGE))
                .setPositiveButton(R.string.ok) { d, _ -> d.dismiss() }
                .create()
    }

    companion object {

        private const val KEY_MESSAGE = "message"

        fun make(message: String) =
                TlsErrorDialogFragment()
                        .also { it.arguments = bundleOf(Pair(KEY_MESSAGE, message)) }
    }
}