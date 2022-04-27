/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.view.scroll.usecase

import androidx.compose.foundation.gestures.ScrollableState
import androidx.lifecycle.LifecycleOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.view.scroll.StateScrollerFactory

class ScrollerUseCase(
    private val contentViewModel: ContentViewModel?,
    private val scrollState: ScrollableState
) {

    operator fun invoke(viewLifecycleOwner: LifecycleOwner) {
        val scroller = StateScrollerFactory().invoke(scrollState)
        contentViewModel?.toTop?.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled() ?: return@observe
            scroller.toTop()
        })
        contentViewModel?.toBottom?.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled() ?: return@observe
            scroller.toTop()
        })
    }

}