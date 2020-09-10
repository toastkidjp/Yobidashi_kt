/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.list

import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.article.list.sort.Sort
import jp.toastkid.article_viewer.tokenizer.NgramTokenizer
import jp.toastkid.lib.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
class ArticleSearchUseCase(
        private val listLoaderUseCase: ListLoaderUseCase,
        private val repository: ArticleRepository,
        private val preferencesWrapper: PreferenceApplier
) {

    private val tokenizer = NgramTokenizer()

    fun all() {
        listLoaderUseCase { Sort.findByName(preferencesWrapper.articleSort()).invoke(repository) }
    }

    fun search(keyword: String?) {
        if (keyword.isNullOrBlank()) {
            return
        }

        listLoaderUseCase { repository.search("${tokenizer(keyword, 2)}") }
    }

    fun filter(keyword: String?) {
        if (!preferencesWrapper.useTitleFilter()) {
            return
        }

        if (keyword.isNullOrBlank()) {
            all()
            return
        }

        listLoaderUseCase { repository.filter(keyword) }
    }

    fun dispose() {
        listLoaderUseCase.dispose()
    }

}