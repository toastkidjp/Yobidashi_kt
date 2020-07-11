/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.detail.subhead

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.toastkid.lib.lifecycle.Event

/**
 * @author toastkidjp
 */
class SubheadDialogFragmentViewModel : ViewModel() {

    private val _subhead = MutableLiveData<Event<String>>()

    val subhead: LiveData<Event<String>> = _subhead

    fun subhead(subhead: String) {
        _subhead.postValue(Event(subhead))
    }

}