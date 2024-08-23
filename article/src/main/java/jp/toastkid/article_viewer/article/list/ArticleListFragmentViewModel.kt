/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.list

import androidx.compose.runtime.mutableStateOf
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.article.list.sort.Sort
import jp.toastkid.article_viewer.bookmark.repository.BookmarkRepository
import jp.toastkid.article_viewer.tokenizer.NgramTokenizer
import jp.toastkid.lib.preference.PreferenceApplier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * @author toastkidjp
 */
class ArticleListFragmentViewModel(
    private val articleRepository: ArticleRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val preferencesWrapper: PreferenceApplier
) {

    private val tokenizer = NgramTokenizer()

    private val _progressVisibility = mutableStateOf(false)

    fun progressVisibility() = _progressVisibility.value

    fun showProgress() {
        _progressVisibility.value = true
    }

    fun hideProgress() {
        _progressVisibility.value = false
    }

    fun sort(sort: Sort) {
        setNextPager { sort.invoke(articleRepository) }
    }

    private val searchResult = mutableStateOf("")

    fun searchResult() = searchResult.value

    private val _dataSource = mutableStateOf<Pager<Int, SearchResult>?>(null)

    fun dataSource(): Flow<PagingData<SearchResult>> = (_dataSource.value?.flow ?: emptyFlow())

    fun search(keyword: String?) {
        setNextPager {
            if (keyword.isNullOrBlank()) {
                val pagingSource = Sort.findByName(preferencesWrapper.articleSort()).invoke(articleRepository)
                searchResult.value = "All article"
                pagingSource
            } else {
                val start = System.currentTimeMillis()
                val pagingSource = articleRepository.search("${tokenizer(keyword, 2)}")
                searchResult.value = "Search ended. [${System.currentTimeMillis() - start}ms]"
                pagingSource
            }
        }
    }

    fun filter(keyword: String?) {
        setNextPager {
            if (keyword.isNullOrBlank())
                Sort.findByName(preferencesWrapper.articleSort())
                    .invoke(articleRepository)
            else
                articleRepository.filter(keyword)
        }
    }

    fun bookmark() {
        val findByIds = articleRepository.findByIds(bookmarkRepository.allArticleIds())

        setNextPager { findByIds }
    }

    private fun setNextPager(pagingSourceFactory: () -> PagingSource<Int, SearchResult>) {
        _dataSource.value = Pager(
            PagingConfig(pageSize = 10, enablePlaceholders = true),
            pagingSourceFactory = pagingSourceFactory
        )
    }

    private val searchInput = mutableStateOf("")

    fun searchInput() = searchInput.value

    fun setSearchInput(newInput: String) {
        searchInput.value = newInput
    }

    private val openSortDialog = mutableStateOf(false)

    fun isOpenSortDialog() = openSortDialog.value

    fun openSortDialog() {
        openSortDialog.value = true
    }

    fun closeSortDialog() {
        openSortDialog.value = false
    }

    private val openDateDialog = mutableStateOf(false)

    fun isOpenDateDialog() = openDateDialog.value

    fun openDataDialog() {
        openDateDialog.value = true
    }

    fun closeDataDialog() {
        openDateDialog.value = false
    }

}