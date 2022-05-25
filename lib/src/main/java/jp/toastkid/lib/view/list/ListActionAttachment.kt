/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.view.list

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.view.scroll.usecase.ScrollerUseCase
import jp.toastkid.lib.viewmodel.PageSearcherViewModel

class ListActionAttachment(
    private val contentViewModel: ContentViewModel?,
    private val pageSearcherViewModel: PageSearcherViewModel?
) {

    operator fun <T> invoke(
        scrollableState: ScrollableState,
        lifecycleOwner: LifecycleOwner,
        listItemState: SnapshotStateList<T>,
        fullItems: Collection<T>,
        predicate: (T, String) -> Boolean
    ) {
        ScrollerUseCase(contentViewModel, scrollableState).invoke(lifecycleOwner)
        pageSearcherViewModel?.find?.observe(lifecycleOwner, {
            val word = it.getContentIfNotHandled() ?: return@observe
            listItemState.clear()
            if (word.isBlank()) {
                listItemState.addAll(fullItems)
                return@observe
            }

            listItemState.addAll(
                fullItems.filter { item -> predicate(item, word) }
            )
        })
    }

    companion object {

        fun make(owner: ViewModelStoreOwner): ListActionAttachment {
            val viewModelProvider = ViewModelProvider(owner)
            return ListActionAttachment(
                viewModelProvider.get(ContentViewModel::class.java),
                viewModelProvider.get(PageSearcherViewModel::class.java)
            )
        }

    }

}