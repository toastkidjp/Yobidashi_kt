/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.view.scroll

import androidx.compose.foundation.lazy.LazyListState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LazyListStateScroller(private val scrollState: LazyListState?): StateScroller {

    override fun toTop() {
        CoroutineScope(Dispatchers.Main).launch {
            scrollState?.scrollToItem(0, 0)
        }
    }

    override fun toBottom() {
        CoroutineScope(Dispatchers.Main).launch {
            scrollState?.scrollToItem(scrollState.layoutInfo.totalItemsCount, 0)
        }
    }

}