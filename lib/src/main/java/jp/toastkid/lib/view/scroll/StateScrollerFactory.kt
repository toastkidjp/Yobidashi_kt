/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.view.scroll

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.lazy.LazyListState

class StateScrollerFactory {

    private val noopScroller = object : StateScroller {
        override fun toTop() = Unit

        override fun toBottom() = Unit
    }

    operator fun invoke(state: ScrollableState): StateScroller {
        return when (state) {
            is LazyListState -> LazyListStateScroller(state)
            is ScrollState -> ScrollStateScroller(state)
            else -> noopScroller
        }
    }

}