/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.editor

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserFragment
import jp.toastkid.yobidashi.libs.HtmlCompat

/**
 * Clear text confirmation dialog.
 *
 * @author toastkidjp
 */
class ClearTextDialogFragment : DialogFragment() {

    interface Callback {
        fun onClickClearInput()
    }

    /**
     * Callback of clicked positive button.
     */
    private var callback: Callback? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        val targetFragment = targetFragment
        if (targetFragment is Callback) {
            callback = targetFragment
        }

        return AlertDialog.Builder(activityContext)
                .setTitle(activityContext.getString(R.string.title_clear_text))
                .setMessage(HtmlCompat.fromHtml(activityContext.getString(R.string.confirm_clear_all_settings)))
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .setPositiveButton(R.string.ok) { d, _ ->
                    callback?.onClickClearInput()
                    d.dismiss()
                }
                .create()
    }

    companion object {

        /**
         * Show this dialog.
         *
         * @param context [Context]
         */
        fun show(context: Context) {
            val dialogFragment = ClearTextDialogFragment()

            if (context is FragmentActivity) {
                val supportFragmentManager = context.supportFragmentManager
                val target = supportFragmentManager
                        .findFragmentByTag(BrowserFragment::class.java.simpleName)
                dialogFragment.setTargetFragment(target, 1)
                dialogFragment.show(
                        supportFragmentManager,
                        ClearTextDialogFragment::class.java.simpleName
                )
            }
        }
    }
}