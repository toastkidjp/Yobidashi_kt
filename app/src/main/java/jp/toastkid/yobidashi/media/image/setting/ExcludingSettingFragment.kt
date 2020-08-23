/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.setting

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.RightSwipeActionAttachment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentSettingImageExcludingBinding
import jp.toastkid.yobidashi.media.image.list.ImageViewerFragmentViewModel

/**
 * @author toastkidjp
 */
class ExcludingSettingFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentSettingImageExcludingBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_setting_image_excluding,
                container,
                false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferenceApplier = PreferenceApplier(view.context)
        val excludedItems = preferenceApplier.excludedItems()
        if (excludedItems.isEmpty() == true) {
            dismiss()
            return
        }

        binding.excludingItems.layoutManager = LinearLayoutManager(activity)

        val viewModel = ViewModelProvider(this)
                .get(ExcludingSettingFragmentViewModel::class.java)

        viewModel.dismiss.observe(this, Observer {
            dismiss()
        })

        val adapter = Adapter(preferenceApplier, viewModel)
        binding.excludingItems.adapter = adapter
        RightSwipeActionAttachment().invoke(binding.excludingItems) {
            adapter.removeAt(it)
        }
        adapter.addAll(excludedItems)
        adapter.notifyDataSetChanged()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val targetFragment = targetFragment ?: return
        ViewModelProvider(targetFragment).get(ImageViewerFragmentViewModel::class.java)
                .refresh()
    }
}