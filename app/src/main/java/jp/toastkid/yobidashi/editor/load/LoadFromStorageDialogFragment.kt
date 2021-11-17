/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.editor.load

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.toastkid.lib.color.IconColorFinder
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

        val color = IconColorFinder.from(binding.root).invoke()
        CompoundDrawableColorApplier().invoke(color, binding.title)

        binding.list.choiceMode = ListView.CHOICE_MODE_SINGLE
        val files = context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.listFiles(TextFileFilter()) ?: return null
        val adapter = object : ArrayAdapter<File>(
            activityContext,
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            files
        ) {}
        binding.list.adapter = adapter
        binding.list.setOnItemClickListener { _, _, position, _ ->
            parentFragmentManager.setFragmentResult(
                "load_from_storage",
                bundleOf("load_from_storage" to files[position])
            )
            dismiss()
        }
        return binding.root
    }

    companion object {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.dialog_user_agent

    }

}