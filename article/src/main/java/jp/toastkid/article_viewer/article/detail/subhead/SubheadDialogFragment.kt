/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.detail.subhead

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * @author toastkidjp
 */
class SubheadDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val requireContext = requireContext()
        val recyclerView = RecyclerView(requireContext)
        recyclerView.adapter = Adapter(layoutInflater).also {
            it.addAll(arguments?.getStringArrayList(KEY_EXTRA_ITEM))
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext, LinearLayoutManager.VERTICAL, false)
        return recyclerView
    }

    companion object {

        private const val KEY_EXTRA_ITEM = "items"

        fun make(items: List<String>): SubheadDialogFragment {
            return SubheadDialogFragment().also {
                it.arguments = bundleOf(KEY_EXTRA_ITEM to items)
            }
        }

    }

}