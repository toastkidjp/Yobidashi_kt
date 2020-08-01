/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.calendar

import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.lib.ContentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class DateSelectedActionService(
        private val repository: ArticleRepository,
        private val viewModel: ContentViewModel
) {

    private val disposables = Job()

    operator fun invoke(year: Int, month: Int, date: Int) {
        CoroutineScope(Dispatchers.Main).launch(disposables) {
            val article = withContext(Dispatchers.IO) {
                repository.findFirst(TitleFilterGenerator(year, month + 1, date))
            } ?: return@launch
            viewModel.newArticle(article.title)
        }
    }

    fun dispose() {
        disposables.cancel()
    }
}