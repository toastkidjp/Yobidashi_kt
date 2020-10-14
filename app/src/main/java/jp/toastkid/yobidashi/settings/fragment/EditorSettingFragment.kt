/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentSettingEditorBinding
import jp.toastkid.yobidashi.editor.EditorFontSize
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.settings.color.ColorChooserDialogFragment
import jp.toastkid.yobidashi.settings.color.ColorChooserDialogFragmentViewModel

/**
 * Editor setting fragment.
 *
 * @author toastkidjp
 */
class EditorSettingFragment : Fragment() {

    /**
     * View data binding object.
     */
    private lateinit var binding: FragmentSettingEditorBinding

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    /**
     * Initial background color.
     */
    @ColorInt
    private var initialBgColor: Int = 0

    /**
     * Initial font color.
     */
    @ColorInt
    private var initialFontColor: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        val activityContext = context ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(activityContext)
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.also { editorModule ->
            val backgroundColor = preferenceApplier.editorBackgroundColor()
            val fontColor = preferenceApplier.editorFontColor()

            initialBgColor = backgroundColor
            initialFontColor = fontColor

            editorModule.palettes.backgroundPalette.also { picker ->
                picker.addSVBar(editorModule.palettes.backgroundSvbar)
                picker.addOpacityBar(editorModule.palettes.backgroundOpacitybar)
                picker.setOnColorChangedListener { editorModule.palettes.ok.setBackgroundColor(it) }
                picker.color = preferenceApplier.editorBackgroundColor()
            }

            editorModule.palettes.fontPalette.also { picker ->
                picker.addSVBar(editorModule.palettes.fontSvbar)
                picker.addOpacityBar(editorModule.palettes.fontOpacitybar)
                picker.setOnColorChangedListener { editorModule.palettes.ok.setTextColor(it) }
                picker.color = preferenceApplier.editorFontColor()
            }
            editorModule.fragment = this

            editorModule.palettes.ok.setOnClickListener { ok() }
            editorModule.palettes.prev.setOnClickListener { reset() }

            ColorPair(backgroundColor, fontColor).setTo(binding.palettes.ok)

            ColorPair(initialBgColor, initialFontColor).setTo(binding.palettes.prev)

            editorModule.fontSize.adapter = object : BaseAdapter() {
                override fun getCount(): Int = EditorFontSize.values().size

                override fun getItem(position: Int): EditorFontSize
                        = EditorFontSize.values()[position]

                override fun getItemId(position: Int): Long
                        = getItem(position).ordinal.toLong()

                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val item = EditorFontSize.values()[position]

                    if (convertView == null) {
                        val newView = layoutInflater.inflate(
                                android.R.layout.simple_spinner_item,
                                parent,
                                false
                        )

                        val viewHolder = FontSpinnerViewHolder(newView)
                        newView.tag = viewHolder
                        viewHolder.bind(item)
                        return newView
                    }

                    val viewHolder = convertView.tag as? FontSpinnerViewHolder?
                    viewHolder?.bind(item)
                    return convertView
                }
            }
            editorModule.fontSize.setSelection(
                    EditorFontSize.findIndex(preferenceApplier.editorFontSize())
            )
            editorModule.fontSize.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    preferenceApplier.setEditorFontSize(EditorFontSize.values()[position].size)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }

            val context = binding.root.context
            editorModule.cursorPreview.setBackgroundColor(preferenceApplier.editorCursorColor(ContextCompat.getColor(context, R.color.editor_cursor)))
            editorModule.highlightPreview.setBackgroundColor(preferenceApplier.editorHighlightColor(ContextCompat.getColor(context, R.color.light_blue_200_dd)))
        }
    }

    /**
     * OK button's action.
     */
    private fun ok() {
        val backgroundColor = binding.palettes.backgroundPalette.color
        val fontColor = binding.palettes.fontPalette.color

        preferenceApplier.setEditorBackgroundColor(backgroundColor)
        preferenceApplier.setEditorFontColor(fontColor)

        binding.palettes.backgroundPalette.color = backgroundColor
        binding.palettes.fontPalette.color = fontColor

        val colorPair = ColorPair(backgroundColor, fontColor)
        colorPair.setTo(binding.palettes.ok)
        Toaster.snackShort(binding.root, R.string.settings_color_done_commit, colorPair)
    }

    /**
     * Reset button's action.
     */
    private fun reset() {
        preferenceApplier.setEditorBackgroundColor(initialBgColor)
        preferenceApplier.setEditorFontColor(initialFontColor)

        binding.palettes.backgroundPalette.color = initialBgColor
        binding.palettes.fontPalette.color = initialFontColor

        ColorPair(initialBgColor, initialFontColor).setTo(binding.palettes.ok)

        Toaster.snackShort(binding.root, R.string.settings_color_done_reset, preferenceApplier.colorPair())
    }

    fun showCursorColorSetting() {
        val activity = requireActivity()
        ColorChooserDialogFragment.withCurrentColor(preferenceApplier.editorCursorColor(ContextCompat.getColor(activity, R.color.editor_cursor)))
                .show(
                        activity.supportFragmentManager,
                        ColorChooserDialogFragment::class.java.canonicalName
                )
        ViewModelProvider(activity)
                .get(ColorChooserDialogFragmentViewModel::class.java)
                .color
                .observe(activity, Observer {
                    preferenceApplier.setEditorCursorColor(it)
                    binding.cursorPreview.setBackgroundColor(it)
                })
    }

    fun showHighlightColorSetting() {
        val activity = requireActivity()
        ColorChooserDialogFragment.withCurrentColor(preferenceApplier.editorHighlightColor(ContextCompat.getColor(activity, R.color.light_blue_200_dd)))
                .show(
                        activity.supportFragmentManager,
                        ColorChooserDialogFragment::class.java.canonicalName
                )
        ViewModelProvider(activity)
                .get(ColorChooserDialogFragmentViewModel::class.java)
                .color
                .observe(activity, Observer {
                    preferenceApplier.setEditorHighlightColor(it)
                    binding.highlightPreview.setBackgroundColor(it)
                })
    }

    companion object : TitleIdSupplier {

        @LayoutRes
        private val LAYOUT_ID = R.layout.fragment_setting_editor

        @StringRes
        override fun titleId() = R.string.subhead_editor

    }
}