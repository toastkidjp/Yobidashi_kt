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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.HtmlCompat

/**
 * Dialog fragment of confirming clear bookmark.
 *
 * @author toastkidjp
 */
class BookmarkClearDialogFragment : DialogFragment() {

    /**
     * Callback interface.
     */
    interface OnClickBookmarkClearCallback {
        fun onClickBookmarkClear()
    }

    /**
     * Callback instance, this is initialized with context.
     */
    private var onClickBookmarkClear: OnClickBookmarkClearCallback? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val target = targetFragment ?: return super.onCreateDialog(savedInstanceState)
        if (target is OnClickBookmarkClearCallback) {
            onClickBookmarkClear = target
        }

        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)
        return AlertDialog.Builder(activityContext)
                .setTitle(R.string.title_clear_bookmark)
                .setMessage(HtmlCompat.fromHtml(getString(R.string.confirm_clear_all_settings)))
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .setPositiveButton(R.string.ok) { d, _ ->
                    onClickBookmarkClear?.onClickBookmarkClear()
                    d.dismiss()
                }
                .create()
    }
}