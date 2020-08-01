/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.color

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.DialogColorChooserBinding

/**
 * @author toastkidjp
 */
class ColorChooserDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        val chooserDialogFragmentViewModel =
                ViewModelProvider(requireActivity())
                        .get(ColorChooserDialogFragmentViewModel::class.java)

        val binding = DataBindingUtil.inflate<DialogColorChooserBinding>(
                LayoutInflater.from(activityContext),
                R.layout.dialog_color_chooser,
                null,
                false
        )

        binding.palette.also {
            it.addSVBar(binding?.svBar)
            it.addOpacityBar(binding?.opacityBar)
            it.setOnColorChangedListener { color ->
                binding.preview.setBackgroundColor(color)
            }
        }

        arguments?.getInt(KEY_CURRENT_COLOR)
                ?.let { setPassedColor(binding, it) }

        return AlertDialog.Builder(activityContext)
                .setTitle(R.string.title_dialog_color_chooser)
                .setView(binding.root)
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .setPositiveButton(R.string.ok) { d, _ ->
                    chooserDialogFragmentViewModel.postColor(binding.palette.color)
                    d.dismiss()
                }
                .create()
    }

    private fun setPassedColor(binding: DialogColorChooserBinding, color: Int) {
        binding.palette.color = color
        binding.preview.setBackgroundColor(color)
    }

    companion object {

        private const val KEY_CURRENT_COLOR = "current_color"

        fun withCurrentColor(@ColorInt currentColor: Int) =
                ColorChooserDialogFragment().also {
                    it.arguments = bundleOf(KEY_CURRENT_COLOR to currentColor)
                }
    }

}