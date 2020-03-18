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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.HtmlCompat

/**
 * [DialogFragment] for confirmation clear search history.
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

        val target = targetFragment
        if (target is OnClickSearchHistoryClearCallback) {
            onClick = target
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

    companion object {
        fun show(fragmentManager: FragmentManager, targetFragment: Fragment) {
            val dialogFragment = SearchHistoryClearDialogFragment()
            dialogFragment.setTargetFragment(targetFragment, 1)
            dialogFragment.show(
                    fragmentManager,
                    SearchHistoryClearDialogFragment::class.java.canonicalName
            )
        }
    }
}