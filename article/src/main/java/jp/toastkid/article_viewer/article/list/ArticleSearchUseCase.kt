/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.list

import jp.toastkid.article_viewer.tokenizer.NgramTokenizer
import jp.toastkid.lib.preference.PreferenceApplier
import kotlinx.coroutines.Job

/**
 * @author toastkidjp
 */
class ArticleSearchUseCase(
        private val adapter: Adapter,
        private val preferencesWrapper: PreferenceApplier
) {

    private val tokenizer = NgramTokenizer()

    /**
     * [CompositeDisposable].
     */
    private val disposables = Job()

    fun search(keyword: String?) {
        if (keyword.isNullOrBlank()) {
            return
        }

        adapter.search("${tokenizer(keyword, 2)}")
    }

    fun filter(keyword: String?) {
        if (!preferencesWrapper.useTitleFilter()) {
            return
        }

        if (keyword.isNullOrBlank()) {
            all()
            return
        }

        adapter.filter(keyword)
    }

    fun all() {
        adapter.all()
    }

    fun dispose() {
        disposables.cancel()
    }

}