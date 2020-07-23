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
import jp.toastkid.lib.lifecycle.Event

/**
 * @author toastkidjp
 */
class ArticleListFragmentViewModel : ViewModel() {

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

}