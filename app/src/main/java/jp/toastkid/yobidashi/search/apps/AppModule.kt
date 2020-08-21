/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.apps

import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import jp.toastkid.yobidashi.databinding.ModuleSearchAppsBinding
import jp.toastkid.yobidashi.launcher.Adapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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
    private var disposable: Job? = null

    private val disposables: Job by lazy { Job() }

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
        if (key.isEmpty()) {
            hide()
            return
        }

        disposable?.cancel()
        disposable = CoroutineScope(Dispatchers.Default).launch {
            adapter.filter(key, 5) { onResult() }
        }
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
        }
    }

    /**
     * Hide this module.
     */
    fun hide() {
        if (binding.root.isVisible) {
            runOnMainThread { binding.root.isVisible = false }
        }
    }

    private fun runOnMainThread(action: () -> Unit) =
            CoroutineScope(Dispatchers.Main).launch(disposables) { action() }

    /**
     * Dispose last query's disposable.
     */
    fun dispose() {
        disposable?.cancel()
        disposables.cancel()
    }

}
