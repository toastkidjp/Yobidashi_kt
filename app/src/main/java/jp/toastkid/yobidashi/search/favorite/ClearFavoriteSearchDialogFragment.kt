/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.favorite

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.HtmlCompat

/**
 * @author toastkidjp
 */
class ClearFavoriteSearchDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        val targetFragment = targetFragment ?: return super.onCreateDialog(savedInstanceState)

        return AlertDialog.Builder(activityContext)
                .setTitle(R.string.title_delete_all)
                .setMessage(HtmlCompat.fromHtml(getString(R.string.confirm_clear_all_settings)))
                .setCancelable(true)
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .setPositiveButton(R.string.ok) { d, _ ->
                    ViewModelProvider(targetFragment)
                            .get(FavoriteSearchFragmentViewModel::class.java)
                            .clear()
                    d.dismiss()
                }
                .create()
    }

    companion object {

        fun show(fragmentManager: FragmentManager, target: Fragment) {
            val dialogFragment = ClearFavoriteSearchDialogFragment()
            dialogFragment.setTargetFragment(target, 1)
            dialogFragment.show(
                    fragmentManager,
                    ClearFavoriteSearchDialogFragment::class.java.canonicalName
            )
        }
    }
}
