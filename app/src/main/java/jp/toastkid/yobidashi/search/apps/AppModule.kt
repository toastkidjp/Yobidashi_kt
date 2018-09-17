/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.apps

import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.databinding.ModuleSearchAppsBinding
import jp.toastkid.yobidashi.launcher.Adapter
import jp.toastkid.yobidashi.libs.facade.BaseModule
import timber.log.Timber

/**
 * @author toastkidjp
 */
class AppModule(binding: ModuleSearchAppsBinding) : BaseModule(binding.root) {

    /**
     * Suggest ModuleAdapter.
     */
    private val adapter: Adapter = Adapter(
            context(),
            binding.root
    )

    init {
        binding.searchApps.layoutManager = LinearLayoutManager(context())
        binding.searchApps.adapter = adapter
    }

    /**
     * Request web API.
     * @param key
     */
    fun request(key: String) {
        if (TextUtils.isEmpty(key)) {
            return
        }

        Completable.fromAction { adapter.filter(key, 5, { onResult() }) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {},
                        Timber::e
                )
    }

    private fun onResult() {
        if (adapter.itemCount == 0) {
            hide()
            return
        }
        show()
    }

    companion object {

        /**
         * Suggest cache capacity.
         */
        private const val SUGGESTION_CACHE_CAPACITY = 30
    }
}
