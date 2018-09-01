/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.history

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.Html
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.HtmlCompat

/**
 * [DialogFragment] for comfirmation clear search history.
 *
 * @author toastkidjp
 */
class SearchHistoryClearDialogFragment : DialogFragment() {

    /**
     * Callback.
     */
    interface OnClickSearchHistoryClearCallback {
        fun onClickSearchHistoryClear()
    }

    /**
     * Received callback.
     */
    private var onClick: OnClickSearchHistoryClearCallback? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)
        if (activityContext is OnClickSearchHistoryClearCallback) {
            onClick = activityContext
        }
        return AlertDialog.Builder(activityContext)
                .setTitle(R.string.title_clear_search_history)
                .setMessage(HtmlCompat.fromHtml(getString(R.string.confirm_clear_all_settings)))
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .setPositiveButton(R.string.ok) { d, _ ->
                    onClick?.onClickSearchHistoryClear()
                    d.dismiss()
                }
                .setCancelable(true)
                .create()
    }
}