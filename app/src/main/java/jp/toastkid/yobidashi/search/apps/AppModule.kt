/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.apps

import android.text.TextUtils
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.databinding.ModuleSearchAppsBinding
import jp.toastkid.yobidashi.launcher.Adapter
import timber.log.Timber

/**
 * App search module in [SearchActivity].
 *
 * @param binding [ModuleSearchAppsBinding]
 * @author toastkidjp
 */
class AppModule(private val binding: ModuleSearchAppsBinding) {

    /**
     * Suggest ModuleAdapter.
     */
    private val adapter: Adapter = Adapter(binding.root.context, binding.root)

    /**
     * Disposable of last query.
     */
    private var disposable: Disposable? = null

    private val disposables = CompositeDisposable()

    var enable = false

    init {
        binding.searchApps.layoutManager = LinearLayoutManager(binding.root.context)
        binding.searchApps.adapter = adapter
    }

    /**
     * Request app search.
     *
     * @param key search keyword.
     */
    fun request(key: String) {
        if (TextUtils.isEmpty(key)) {
            hide()
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
     * Show this module.
     */
    fun show() {
        if (!binding.root.isVisible && enable) {
            runOnMainThread { binding.root.isVisible = true }
                    .addTo(disposables)
        }
    }

    /**
     * Hide this module.
     */
    fun hide() {
        if (binding.root.isVisible) {
            runOnMainThread { binding.root.isVisible = false }
                    .addTo(disposables)
        }
    }

    private fun runOnMainThread(action: () -> Unit) =
            Completable.fromAction { action() }
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {},
                            Timber::e
                    )

    /**
     * Dispose last query's disposable.
     */
    fun dispose() {
        disposable?.dispose()
        disposables.clear()
    }

}
