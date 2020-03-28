/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.history

import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import jp.toastkid.yobidashi.databinding.ModuleSearchHistoryBinding
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import timber.log.Timber

/**
 * Search history module.
 *
 * @param binding Data binding object
 * @param searchCallback
 * @param onClickAdd
 *
 * @author toastkidjp
 */
class HistoryModule(
        private val binding: ModuleSearchHistoryBinding,
        searchCallback: (SearchHistory) -> Unit,
        onClickAdd: (SearchHistory) -> Unit
) {

    /**
     * RecyclerView's moduleAdapter.
     */
    private val moduleAdapter: ModuleAdapter

    var enable: Boolean = true

    /**
     * Last subscription.
     */
    private var disposable: Disposable? = null

    /**
     * Use for disposing.
     */
    private val disposables: CompositeDisposable = CompositeDisposable()

    init {
        binding.module = this

        val context = binding.root.context
        binding.searchHistories.layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        val repository = DatabaseFinder().invoke(context).searchHistoryRepository()

        moduleAdapter = ModuleAdapter(
                context,
                repository,
                searchCallback,
                { visible -> if (visible) { show() } else { hide() } },
                { history -> onClickAdd(history) },
                true,
                5
        )
        binding.searchHistories.adapter = moduleAdapter

        Completable.fromAction { SwipeActionAttachment().invoke(binding.searchHistories) }
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe()
                .addTo(disposables)
    }

    /**
     * Query table with passed word.
     *
     * @param s query string
     */
    fun query(s: CharSequence) {
        disposable?.dispose()
        disposable = moduleAdapter.query(s)
    }

    /**
     * Clear search history.
     */
    fun confirmClear() {
        val activityContext = binding.root.context
        if (activityContext is FragmentActivity) {
            ClearSearchHistoryDialogFragment().show(
                    activityContext.supportFragmentManager,
                    ClearSearchHistoryDialogFragment::class.java.simpleName
            )
        }
    }

    fun clear() {
        moduleAdapter.clear()
        hide()
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
     * Dispose last subscription.
     */
    fun dispose() {
        disposable?.dispose()
    }

}
