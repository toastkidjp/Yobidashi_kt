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
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.yobidashi.R
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.yobidashi.libs.clip.Clipboard

/**
 * @author toastkidjp
 */
class AnchorTypeLongTapDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        val url = arguments?.getString(KEY_EXTRA)
                ?: return super.onCreateDialog(savedInstanceState)

        val title = arguments?.getString(KEY_TITLE) ?: ""

        val viewModel = ViewModelProvider(requireActivity()).get(BrowserViewModel::class.java)

        val uri = url.toUri()

        return AlertDialog.Builder(activityContext)
                .setTitle("$title URL: $url")
                .setItems(R.array.url_menu, { _, which ->
                    when (which) {
                        0 -> viewModel.open(uri)
                        1 -> viewModel.openBackground(title, uri)
                        2 -> viewModel.preview(uri)
                        3 -> Clipboard.clip(activityContext, url)
                        4 -> Clipboard.clip(activityContext, title)
                    }
                })
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .create()
    }

    companion object {

        private const val KEY_TITLE = "title"

        private const val KEY_EXTRA = "extra"

        fun make(title: String, extra: String) =
                AnchorTypeLongTapDialogFragment()
                        .also {
                            it.arguments = bundleOf(
                                    KEY_TITLE to title,
                                    KEY_EXTRA to extra
                            )
                        }
    }
}