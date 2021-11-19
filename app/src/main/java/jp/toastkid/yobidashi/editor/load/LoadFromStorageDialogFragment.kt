/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.editor.load

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.toastkid.lib.color.IconColorFinder
import jp.toastkid.lib.io.TextFileFilter
import jp.toastkid.lib.view.CompoundDrawableColorApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.DialogUserAgentBinding
import java.io.File

class LoadFromStorageDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activityContext = context
            ?: return super.onCreateView(inflater, container, savedInstanceState)

        val binding: DialogUserAgentBinding =
            DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)

        initializeTitle(binding.title)

        initializeList(activityContext, binding.list)

        return binding.root
    }

    private fun initializeTitle(titleView: TextView) {
        val color = IconColorFinder.from(titleView.context).invoke()
        CompoundDrawableColorApplier().invoke(color, titleView)

        titleView.setText(R.string.load_from_storage)
        titleView.setCompoundDrawables(
            ContextCompat.getDrawable(titleView.context, R.drawable.ic_load),
            null, null, null
        )
    }

    private fun initializeList(activityContext: Context, listView: ListView) {
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE
        val files = context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.listFiles(
            TextFileFilter()
        ) ?: return
        val adapter = object : ArrayAdapter<File>(
            activityContext,
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            files
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                view.findViewById<TextView>(android.R.id.text1).text = files[position].name
                return view
            }
        }
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            parentFragmentManager.setFragmentResult(
                "load_from_storage",
                bundleOf("load_from_storage" to files[position])
            )
            dismiss()
        }
    }

    companion object {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.dialog_user_agent

    }

}