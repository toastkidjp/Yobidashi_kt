/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author toastkidjp
 */
class AppBarViewModel : ViewModel() {

    private val appBarComposable = mutableStateOf<@Composable () -> Unit>({})

    val appBarContent: State<@Composable () -> Unit> = appBarComposable

    private val _content = MutableLiveData<View>()

    val content: LiveData<View> = _content

    @Deprecated("This function will be deleted.")
    fun replace(view: View) {
        _content.postValue(view)
    }

    fun replace(composable: @Composable() () -> Unit) {
        appBarComposable.value = composable
    }

    private val _visibility = MutableLiveData<Boolean>()

    val visibility: LiveData<Boolean> = _visibility

    fun show() = _visibility.postValue(true)

    fun hide() = _visibility.postValue(false)

}