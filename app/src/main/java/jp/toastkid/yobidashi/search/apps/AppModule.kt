/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.apps

import androidx.recyclerview.widget.LinearLayoutManager
import android.text.TextUtils
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.databinding.ModuleSearchAppsBinding
import jp.toastkid.yobidashi.launcher.Adapter
import jp.toastkid.yobidashi.libs.facade.BaseModule
import timber.log.Timber

/**
 * App search module in [SearchActivity].
 *
 * @param binding [ModuleSearchAppsBinding]
 * @author toastkidjp
 */
class AppModule(binding: ModuleSearchAppsBinding) : BaseModule(binding.root) {

    /**
     * Suggest ModuleAdapter.
     */
    private val adapter: Adapter = Adapter(context(), binding.root)

    /**
     * Disposable of last query.
     */
    private var disposable: Disposable? = null

    init {
        binding.searchApps.layoutManager = LinearLayoutManager(context())
        binding.searchApps.adapter = adapter
    }

    /**
     * Request app search.
     *
     * @param key search keyword.
     */
    fun request(key: String) {
        if (TextUtils.isEmpty(key)) {
            return
        }

        disposable?.dispose()
        disposable = Completable.fromAction { adapter.filter(key, 5, { onResult() }) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {},
                        Timber::e
                )
    }

    /**
     * Result action.
     */
    private fun onResult() {
        if (adapter.itemCount == 0) {
            hide()
            return
        }
        show()
    }

    /**
     * Dispose last query's disposable.
     */
    fun dispose() {
        disposable?.dispose()
    }

}
