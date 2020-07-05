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
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.article.data.AppDatabase
import jp.toastkid.article_viewer.article.detail.ContentViewerFragment
import jp.toastkid.article_viewer.databinding.FragmentCalendarBinding
import jp.toastkid.lib.ContentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class CalendarFragment : Fragment() {

    private lateinit var binding: FragmentCalendarBinding

    private lateinit var articleRepository: ArticleRepository

    private var contentViewModel: ContentViewModel? = null

    private val disposables = Job()

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

        contentViewModel = activity?.let {
            ViewModelProvider(it).get(ContentViewModel::class.java)
        }

        articleRepository = AppDatabase.find(activityContext).diaryRepository()

        setSelectedAction()
    }

    private fun setSelectedAction() {
        binding.calendar.setOnDateChangeListener { _, year, month, date ->
            CoroutineScope(Dispatchers.Main).launch(disposables) {
                val article = withContext(Dispatchers.IO) {
                    // TODO make efficient.
                    articleRepository.findFirst(TitleFilterGenerator(year, month + 1, date))
                } ?: return@launch
                contentViewModel?.newArticle(article.title)
            }
        }
    }

    override fun onDetach() {
        disposables.cancel()
        super.onDetach()
    }

}