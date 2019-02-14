/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.bookmark

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
class DefaultBookmarkDialogFragment : DialogFragment() {

    interface OnClickDefaultBookmarkCallback {
        fun onClickAddDefaultBookmark()
    }

    private var onClick: OnClickDefaultBookmarkCallback? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        if (activityContext is OnClickDefaultBookmarkCallback) {
            onClick = activityContext
        }

        return AlertDialog.Builder(activityContext)
                .setTitle(R.string.title_add_default_bookmark)
                .setMessage(R.string.message_add_default_bookmark)
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .setPositiveButton(R.string.ok) { d, _ ->
                    onClick?.onClickAddDefaultBookmark()
                    d.dismiss()
                }
                .setCancelable(true)
                .create()
    }
}