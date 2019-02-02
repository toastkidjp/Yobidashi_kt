/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.history

import android.os.Handler
import android.os.Looper
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import jp.toastkid.yobidashi.databinding.ModuleSearchHistoryBinding
import jp.toastkid.yobidashi.libs.db.DbInitializer
import jp.toastkid.yobidashi.libs.facade.BaseModule

/**
 * Search history module.
 *
 * @param binding Data binding object
 * @param searchCallback
 * @param onTouch
 * @param onClickAdd
 *
 * @author toastkidjp
 */
class HistoryModule(
        private val binding: ModuleSearchHistoryBinding,
        searchCallback: (SearchHistory) -> Unit,
        onClickAdd: (SearchHistory) -> Unit
) : BaseModule(binding.root) {

    /**
     * RecyclerView's moduleAdapter.
     */
    private val moduleAdapter: ModuleAdapter

    /**
     * Database relation.
     */
    private val relation: SearchHistory_Relation

    /**
     * Last subscription.
     */
    private var disposable: Disposable? = null

    /**
     * Use for disposing.
     */
    private val disposables: CompositeDisposable = CompositeDisposable()

    /**
     * For executing on UI thread.
     */
    private val uiThreadHandler = Handler(Looper.getMainLooper())

    init {
        binding.module = this

        relation = DbInitializer.init(context()).relationOfSearchHistory()

        binding.searchHistories.layoutManager =
                LinearLayoutManager(context(), LinearLayoutManager.VERTICAL, false)
        moduleAdapter = ModuleAdapter(
                context(),
                relation,
                searchCallback,
                { visible -> if (visible) { show() } else { hide() } },
                { history -> onClickAdd(history) }
        )
        binding.searchHistories.adapter = moduleAdapter

        Completable.fromAction { SwipeActionAttachment.invoke(binding.searchHistories) }
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
        val activityContext = context()
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
     * Dispose last subscription.
     */
    fun dispose() {
        disposable?.dispose()
    }

}
