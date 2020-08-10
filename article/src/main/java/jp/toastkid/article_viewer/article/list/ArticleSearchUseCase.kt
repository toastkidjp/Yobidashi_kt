/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.list

import androidx.annotation.UiThread
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.tokenizer.NgramTokenizer
import jp.toastkid.lib.preference.PreferenceApplier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class ArticleSearchUseCase(
        private val articleRepository: ArticleRepository,
        private val viewModel: ArticleListFragmentViewModel?,
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

    private suspend fun applyArticle(results: List<SearchResult>) {
        adapter.clear()
        setSearchStart()

        val start = System.currentTimeMillis()

        withContext(Dispatchers.Default) {
            results.forEach(adapter::add)
        }

        adapter.notifyDataSetChanged()
        viewModel?.hideProgress()
        setSearchEnded(results.size, System.currentTimeMillis() - start)
    }

    private fun setSearchStart() {
        viewModel?.showProgress()
        viewModel?.setProgressMessageId(R.string.message_search_in_progress)
    }

    @UiThread
    private fun setSearchEnded(itemCount: Int, duration: Long) {
        viewModel?.hideProgress()
        viewModel?.setProgressMessage("$itemCount Articles / $duration[ms]")
    }

    fun dispose() {
        disposables.cancel()
    }

}