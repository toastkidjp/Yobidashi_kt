/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.reader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author toastkidjp
 */
class ReaderFragmentViewModel : ViewModel() {

    private val _content = MutableLiveData<Pair<String, String>>()

    val content: LiveData<Pair<String, String>> = _content

    fun setContent(title: String, content: String) {
        _content.postValue(title to content)
    }

}