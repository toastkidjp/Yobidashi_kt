/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.list.date

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.article.data.AppDatabase
import jp.toastkid.article_viewer.article.list.ArticleListFragmentViewModel
import jp.toastkid.article_viewer.calendar.DateSelectedActionUseCase
import jp.toastkid.article_viewer.databinding.DialogDateFilterBinding
import jp.toastkid.lib.ContentViewModel
import java.util.Calendar

class DateFilterDialogFragment  : BottomSheetDialogFragment() {

    private lateinit var binding: DialogDateFilterBinding

    private var dateSelectedActionUseCase: DateSelectedActionUseCase? = null

    private var date: Triple<Int, Int, Int>? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_date_filter, container, false)
        binding.dialog = this

        val today = Calendar.getInstance()
        binding.datePicker.init(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
        ) { _, year, monthOfYear, dayOfMonth ->
            date = Triple(year, monthOfYear, dayOfMonth)
        }

        activity?.let {
            dateSelectedActionUseCase = DateSelectedActionUseCase(
                    AppDatabase.find(it).articleRepository(),
                    ViewModelProvider(it).get(ContentViewModel::class.java)
            )
        }

        return binding.root
    }

    fun filterByMonth() {
       targetFragment?.let {
           val formattedMonth = MonthFormatterUseCase().invoke(binding.datePicker.month)
           ViewModelProvider(it).get(ArticleListFragmentViewModel::class.java)
                   .filter("${binding.datePicker.year}-$formattedMonth")
       }

        dismiss()
    }

    fun openDate() {
        dateSelectedActionUseCase?.invoke(
                binding.datePicker.year,
                binding.datePicker.month,
                binding.datePicker.dayOfMonth
        )
        dismiss()
    }

}