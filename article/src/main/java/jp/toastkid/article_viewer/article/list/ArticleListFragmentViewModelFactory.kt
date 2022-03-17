/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.lib.preference.PreferenceApplier

class ArticleListFragmentViewModelFactory(
    private val articleRepository: ArticleRepository,
    private val preferencesWrapper: PreferenceApplier
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ArticleListFragmentViewModel(articleRepository, preferencesWrapper) as T

}