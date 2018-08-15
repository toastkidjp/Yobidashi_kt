/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.webview.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import androidx.core.os.bundleOf
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
class ImageTypeLongTapDialogFragment : DialogFragment() {

    private var onClick: ImageDialogCallback? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        val url = arguments?.getString(KEY_EXTRA)
                ?: return super.onCreateDialog(savedInstanceState)

        val target = targetFragment
        if (target is ImageDialogCallback) {
            onClick = target
        }

        return AlertDialog.Builder(activityContext)
                .setTitle("Image: $url")
                .setItems(R.array.image_menu, { _, which ->
                    when (which) {
                        0 -> onClick?.onClickSetBackground(url)
                        1 -> onClick?.onClickSaveForBackground(url)
                        2 -> onClick?.onClickDownloadImage(url)
                    }
                })
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .create()
    }

    companion object {

        private const val KEY_EXTRA = "extra"

        fun make(extra: String) =
                ImageTypeLongTapDialogFragment()
                        .also { it.arguments = bundleOf(KEY_EXTRA to extra) }
    }
}