/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.main

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author toastkidjp
 */
class FragmentSwitcherViewModel : ViewModel() {

    private val _browser = MutableLiveData<Uri>()

    val browser: LiveData<Uri> = _browser

    fun browser(uri: Uri? = null) {
        _browser.postValue(uri)
    }

    private val _nextEditorFragment = MutableLiveData<String>()

    val nextEditorFragment: LiveData<String> = _nextEditorFragment

    fun openEditor(path: String) {
        _nextEditorFragment.postValue(path)
    }

    private val _pdf = MutableLiveData<Pair<Uri, Int>>()

    val pdf: LiveData<Pair<Uri, Int>> = _pdf

    fun openPdf(uri: Uri, scrollY: Int) {
        _pdf.postValue(uri to scrollY)
    }
}