/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.list.sort

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.article.list.ArticleListFragmentViewModel
import jp.toastkid.article_viewer.databinding.DialogSortSettingBinding
import jp.toastkid.lib.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
class SortSettingDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val activityContext = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)

        val preferenceApplier = PreferenceApplier(activityContext)

        val binding: DialogSortSettingBinding =
                DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)

        binding.list.choiceMode = ListView.CHOICE_MODE_SINGLE
        val currentIndex = Sort.findCurrentIndex(preferenceApplier.articleSort())
        val adapter = object : ArrayAdapter<CharSequence>(
                activityContext,
                android.R.layout.simple_list_item_single_choice,
                android.R.id.text1,
                Sort.titles()
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val isItemChecked = position == currentIndex
                if (isItemChecked) {
                    binding.list.setItemChecked(position, true)
                }
                return view
            }
        }
        binding.list.adapter = adapter
        binding.list.setOnItemClickListener { _, _, position, _ ->
            val sort = Sort.values()[position]
            preferenceApplier.setArticleSort(sort.name)
            targetFragment?.let {
                ViewModelProvider(it).get(ArticleListFragmentViewModel::class.java)
                        .sort(sort)
            }
            dismiss()
        }
        return binding.root
    }

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.dialog_sort_setting

    }
}