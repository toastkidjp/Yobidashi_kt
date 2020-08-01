/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.article.data.AppDatabase
import jp.toastkid.article_viewer.databinding.FragmentCalendarBinding
import jp.toastkid.lib.ContentViewModel

/**
 * @author toastkidjp
 */
class CalendarFragment : Fragment() {

    private lateinit var binding: FragmentCalendarBinding

    private lateinit var dateSelectedActionService: DateSelectedActionService

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_calendar, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activityContext = context ?: return

        dateSelectedActionService = DateSelectedActionService(
                AppDatabase.find(activityContext).articleRepository(),
                ViewModelProvider(requireActivity()).get(ContentViewModel::class.java)
        )

        binding.calendar.setOnDateChangeListener { _, year, month, date ->
            dateSelectedActionService.invoke(year, month, date)
        }
    }

    override fun onDetach() {
        dateSelectedActionService.dispose()
        super.onDetach()
    }

}