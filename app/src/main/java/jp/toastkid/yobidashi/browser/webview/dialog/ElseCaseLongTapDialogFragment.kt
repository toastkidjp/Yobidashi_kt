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
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.search.SearchAction

/**
 * @author toastkidjp
 */
class ElseCaseLongTapDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        val category = arguments?.getString(KEY_SEARCH_ENGINE)
                ?: return super.onCreateDialog(savedInstanceState)

        val extra = arguments?.getString(KEY_EXTRA)
                ?: return super.onCreateDialog(savedInstanceState)

        return AlertDialog.Builder(activityContext)
                .setTitle("Text: $extra")
                .setItems(R.array.url_menu) { dialog, which ->
                    when (which) {
                        0 -> Clipboard.clip(activityContext, extra)
                        1 -> SearchAction(activityContext, category, extra).invoke()
                    }
                }
                .setCancelable(true)
                .setNegativeButton(R.string.cancel) { d, i -> d.cancel() }
                .create()
    }

    companion object {
        private const val KEY_SEARCH_ENGINE = "searchEngine"

        private const val KEY_EXTRA = "extra"

        fun make(searchEngine: String, extra: String) =
                ElseCaseLongTapDialogFragment().also {
                    it.arguments = bundleOf(
                            KEY_SEARCH_ENGINE to searchEngine,
                            KEY_EXTRA to extra
                    )
                }
    }
}