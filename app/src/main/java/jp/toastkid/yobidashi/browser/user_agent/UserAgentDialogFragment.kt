/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.user_agent

import android.content.Context
import android.os.Bundle
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
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.CompoundDrawableColorApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.DialogUserAgentBinding

/**
 * @author toastkidjp
 */
class UserAgentDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activityContext = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)

        val preferenceApplier = PreferenceApplier(activityContext)

        val binding: DialogUserAgentBinding =
                DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)

        val color = IconColorFinder.from(binding.root).invoke()
        CompoundDrawableColorApplier().invoke(color, binding.title)

        initializeList(preferenceApplier, activityContext, binding.list)
        return binding.root
    }

    private fun initializeList(
        preferenceApplier: PreferenceApplier,
        activityContext: Context,
        listView: ListView
    ) {
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE
        val currentIndex = UserAgent.findCurrentIndex(preferenceApplier.userAgent())
        val adapter = object : ArrayAdapter<CharSequence>(
            activityContext,
            android.R.layout.simple_list_item_single_choice,
            android.R.id.text1,
            UserAgent.titles()
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val isItemChecked = position == currentIndex
                if (isItemChecked) {
                    listView.setItemChecked(position, true)
                }
                return view
            }
        }
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            val userAgent = UserAgent.values()[position]
            preferenceApplier.setUserAgent(userAgent.name)
            parentFragmentManager.setFragmentResult(
                "user_agent_setting",
                bundleOf("user_agent_setting" to userAgent)
            )
            dismiss()
        }
    }

    companion object {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.dialog_user_agent

    }
}