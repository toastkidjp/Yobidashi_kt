/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentSettingEditorBinding
import jp.toastkid.yobidashi.editor.EditorFontSize
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.settings.color.ColorChooserDialogFragment
import jp.toastkid.yobidashi.settings.color.ColorChooserDialogFragmentViewModel

/**
 * Editor setting fragment.
 *
 * @author toastkidjp
 */
class EditorSettingFragment : Fragment(), TitleIdSupplier {

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

            editorModule.backgroundPalette.also { picker ->
                picker.addSVBar(editorModule.backgroundSvbar)
                picker.addOpacityBar(editorModule.backgroundOpacitybar)
                picker.setOnColorChangedListener { editorModule.ok.setBackgroundColor(it) }
                picker.color = preferenceApplier.editorBackgroundColor()
            }

            editorModule.fontPalette.also { picker ->
                picker.addSVBar(editorModule.fontSvbar)
                picker.addOpacityBar(editorModule.fontOpacitybar)
                picker.setOnColorChangedListener { editorModule.ok.setTextColor(it) }
                picker.color = preferenceApplier.editorFontColor()
            }
            editorModule.fragment = this

            ColorPair(backgroundColor, fontColor).setTo(binding.ok)

            ColorPair(initialBgColor, initialFontColor).setTo(binding.prev)

            editorModule.fontSize.adapter = object : BaseAdapter() {
                override fun getCount(): Int = EditorFontSize.values().size

                override fun getItem(position: Int): EditorFontSize
                        = EditorFontSize.values()[position]

                override fun getItemId(position: Int): Long
                        = EditorFontSize.values()[position].ordinal.toLong()

                @SuppressLint("ViewHolder")
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val item = EditorFontSize.values()[position]
                    val itemView = LayoutInflater.from(context)
                            .inflate(android.R.layout.simple_spinner_item, parent, false)
                    val textView = itemView.findViewById<TextView>(android.R.id.text1)
                    textView.text = item.size.toString()
                    return itemView
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

            editorModule.cursorPreview.setBackgroundColor(preferenceApplier.editorCursorColor())
            editorModule.highlightPreview.setBackgroundColor(preferenceApplier.editorHighlightColor())
        }
    }

    /**
     * OK button's action.
     */
    fun ok() {
        val backgroundColor = binding.backgroundPalette.color
        val fontColor = binding.fontPalette.color

        preferenceApplier.setEditorBackgroundColor(backgroundColor)
        preferenceApplier.setEditorFontColor(fontColor)

        binding.backgroundPalette.color = backgroundColor
        binding.fontPalette.color = fontColor

        val colorPair = ColorPair(backgroundColor, fontColor)
        colorPair.setTo(binding.ok)
        Toaster.snackShort(binding.root, R.string.settings_color_done_commit, colorPair)
    }

    /**
     * Reset button's action.
     */
    fun reset() {
        preferenceApplier.setEditorBackgroundColor(initialBgColor)
        preferenceApplier.setEditorFontColor(initialFontColor)

        binding.backgroundPalette.color = initialBgColor
        binding.fontPalette.color = initialFontColor

        ColorPair(initialBgColor, initialFontColor).setTo(binding.ok)

        Toaster.snackShort(binding.root, R.string.settings_color_done_reset, preferenceApplier.colorPair())
    }

    fun showCursorColorSetting() {
        val activity = requireActivity()
        ColorChooserDialogFragment.withCurrentColor(preferenceApplier.editorCursorColor())
                .show(
                        activity.supportFragmentManager,
                        ColorChooserDialogFragment::class.java.canonicalName
                )
        ViewModelProviders.of(activity)
                .get(ColorChooserDialogFragmentViewModel::class.java)
                .color
                .observe(activity, Observer {
                    preferenceApplier.setEditorCursorColor(it)
                    binding.cursorPreview.setBackgroundColor(it)
                })
    }

    fun showHighlightColorSetting() {
        val activity = requireActivity()
        ColorChooserDialogFragment.withCurrentColor(preferenceApplier.editorHighlightColor())
                .show(
                        activity.supportFragmentManager,
                        ColorChooserDialogFragment::class.java.canonicalName
                )
        ViewModelProviders.of(activity)
                .get(ColorChooserDialogFragmentViewModel::class.java)
                .color
                .observe(activity, Observer {
                    preferenceApplier.setEditorHighlightColor(it)
                    binding.highlightPreview.setBackgroundColor(it)
                })
    }

    @StringRes
    override fun titleId() = R.string.subhead_editor

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.fragment_setting_editor

    }
}