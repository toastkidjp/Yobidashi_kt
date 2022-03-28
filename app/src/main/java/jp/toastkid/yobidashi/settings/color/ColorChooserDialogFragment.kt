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
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.godaddy.android.colorpicker.ClassicColorPicker
import jp.toastkid.lib.interop.ComposeViewFactory
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
class ColorChooserDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = activity ?: return super.onCreateDialog(savedInstanceState)

        val passedColorInt = arguments?.getInt(KEY_CURRENT_COLOR)
        val passedColor = if (passedColorInt == null) Color.Transparent else Color(passedColorInt)
        val colorState = mutableStateOf(passedColor)

        val view = ComposeViewFactory().invoke(activityContext) {
            val currentBackgroundColor = remember { colorState }

            ClassicColorPicker(
                color = currentBackgroundColor.value,
                onColorChanged = { hsvColor ->
                    currentBackgroundColor.value = hsvColor.toColor()
                },
                modifier = Modifier.height(200.dp)
            )
        }

        return AlertDialog.Builder(activityContext)
                .setTitle(R.string.title_dialog_color_chooser)
                .setView(view)
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .setPositiveButton(R.string.ok) { d, _ ->
                    parentFragmentManager.setFragmentResult(
                        "color",
                        bundleOf("color" to colorState.value.toArgb())
                    )
                    d.dismiss()
                }
                .create()
    }

    companion object {

        private const val KEY_CURRENT_COLOR = "current_color"

        fun withCurrentColor(@ColorInt currentColor: Int) =
                ColorChooserDialogFragment().also {
                    it.arguments = bundleOf(KEY_CURRENT_COLOR to currentColor)
                }
    }

}