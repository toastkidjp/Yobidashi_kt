/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.history.usecase

import androidx.fragment.app.FragmentActivity
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClearItemsUseCase(
    private val adapterClearAll: () -> Unit,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    operator fun invoke(activity: FragmentActivity?) {
        activity ?: return
        CoroutineScope(mainDispatcher).launch {
            withContext(ioDispatcher) {
                DatabaseFinder().invoke(activity).searchHistoryRepository().deleteAll()
            }

            adapterClearAll()
            activity.supportFragmentManager.popBackStack()
        }
    }

}