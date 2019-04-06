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
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.clip.Clipboard

/**
 * @author toastkidjp
 */
class AnchorTypeLongTapDialogFragment : DialogFragment() {

    private var onClick: AnchorDialogCallback? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        val url = arguments?.getString(KEY_EXTRA)
                ?: return super.onCreateDialog(savedInstanceState)

        val target = targetFragment
        if (target is AnchorDialogCallback) {
            onClick = target
        }

        return AlertDialog.Builder(activityContext)
                .setTitle("URL: $url")
                .setItems(R.array.url_menu, { _, which ->
                    when (which) {
                        0 -> onClick?.openNewTab(url)
                        1 -> onClick?.openBackgroundTab(url)
                        2 -> onClick?.openCurrent(url)
                        3 -> onClick?.preview(url)
                        4 -> Clipboard.clip(activityContext, url)
                    }
                })
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .create()
    }

    companion object {

        private const val KEY_EXTRA = "extra"

        fun make(extra: String) =
                AnchorTypeLongTapDialogFragment()
                        .also { it.arguments = bundleOf(KEY_EXTRA to extra) }
    }
}