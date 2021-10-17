/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.favorite.usecase

import androidx.fragment.app.FragmentActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.search.favorite.ModuleAdapter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class ClearItemsUseCase(
    private val adapter: ModuleAdapter?,
    private val showSnackbar: (Int) -> Unit,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    operator fun invoke(activity: FragmentActivity?, disposables: Job) {
        val context = activity ?: return
        val repository = DatabaseFinder().invoke(context).favoriteSearchRepository()

        CoroutineScope(mainDispatcher).launch(disposables) {
            withContext(ioDispatcher) {
                repository.deleteAll()
                adapter?.clear()
            }

            showSnackbar(R.string.settings_color_delete)
            activity.supportFragmentManager.popBackStack()
        }
    }

}