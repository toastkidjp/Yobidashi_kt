/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.article.list.sort.Sort
import jp.toastkid.article_viewer.tokenizer.NgramTokenizer
import jp.toastkid.lib.lifecycle.Event
import jp.toastkid.lib.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
class ArticleListFragmentViewModel(
    private val articleRepository: ArticleRepository,
    private val preferencesWrapper: PreferenceApplier
) : ViewModel() {

    private val tokenizer = NgramTokenizer()

    private val _progressVisibility = MutableLiveData<Event<Boolean>>()
    val progressVisibility : LiveData<Event<Boolean>> = _progressVisibility

    fun showProgress() {
        _progressVisibility.postValue(Event(true))
    }

    fun hideProgress() {
        _progressVisibility.postValue(Event(false))
    }

    private val _progress = MutableLiveData<Event<String>>()
    val progress : LiveData<Event<String>> = _progress

    fun setProgressMessage(message: String) {
        _progress.postValue(Event(message))
    }

    private val _messageId = MutableLiveData<Event<Int>>()
    val messageId : LiveData<Event<Int>> = _messageId

    fun setProgressMessageId(messageId: Int) {
        _messageId.postValue(Event(messageId))
    }

    private val _sort = MutableLiveData<Event<Sort>>()
    val sort: LiveData<Event<Sort>> = _sort

    fun sort(sort: Sort) {
        setNextPager { sort.invoke(articleRepository) }
    }

    private val _dataSource = MutableLiveData<Pager<Int, SearchResult>>()
    val dataSource: LiveData<Pager<Int, SearchResult>> = _dataSource

    fun search(keyword: String?) {
        setNextPager {
            if (keyword.isNullOrBlank())
                Sort.findByName(preferencesWrapper.articleSort()).invoke(articleRepository)
            else
                articleRepository.search("${tokenizer(keyword, 2)}")
        }
    }

    fun filter(keyword: String?) {
        setNextPager {
            if (keyword.isNullOrBlank())
                Sort.findByName(this@ArticleListFragmentViewModel.preferencesWrapper.articleSort())
                    .invoke(this@ArticleListFragmentViewModel.articleRepository)
            else
                this@ArticleListFragmentViewModel.articleRepository.filter(keyword)
        }
    }

    private fun setNextPager(pagingSourceFactory: () -> PagingSource<Int, SearchResult>) {
        _dataSource.postValue(
            Pager(
                PagingConfig(pageSize = 10, enablePlaceholders = true),
                pagingSourceFactory = pagingSourceFactory
            )
        )
    }

}