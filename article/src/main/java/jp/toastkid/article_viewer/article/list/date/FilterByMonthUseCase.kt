/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.list.date

import jp.toastkid.article_viewer.article.list.ArticleListFragmentViewModel

class FilterByMonthUseCase(
        private val articleListFragmentViewModel: ArticleListFragmentViewModel,
        private val monthFormatterUseCase: MonthFormatterUseCase = MonthFormatterUseCase()
) {

    operator fun invoke(year: Int, month: Int) {
        val formattedMonth = monthFormatterUseCase.invoke(month)
        articleListFragmentViewModel.filter("$year-$formattedMonth")
    }

}