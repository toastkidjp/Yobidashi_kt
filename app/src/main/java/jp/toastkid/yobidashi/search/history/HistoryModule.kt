/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.history

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModuleSearchHistoryBinding
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.search.SearchFragmentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Search history module.
 *
 * @param binding Data binding object
 * @param searchCallback
 * @param onClickAdd
 *
 * @author toastkidjp
 */
class HistoryModule
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    /**
     * RecyclerView's moduleAdapter.
     */
    private val moduleAdapter: ModuleAdapter

    var enable: Boolean = true

    private var binding: ModuleSearchHistoryBinding? = null

    /**
     * Last subscription.
     */
    private var disposable: Job? = null

    init {
        val context = context
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.module_search_history,
            this,
            true
        )
        binding?.module = this

        binding?.searchHistories?.layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        val repository = DatabaseFinder().invoke(context).searchHistoryRepository()

        moduleAdapter = ModuleAdapter(
                context,
                repository,
                { if (it) show() else hide() },
                true,
                5
        )
        binding?.searchHistories?.adapter = moduleAdapter

        CoroutineScope(Dispatchers.Main).launch {
            val recyclerView = binding?.searchHistories ?: return@launch
            SwipeActionAttachment().invoke(recyclerView)
        }
    }

    /**
     * Query table with passed word.
     *
     * @param s query string
     */
    fun query(s: CharSequence) {
        disposable?.cancel()
        disposable = moduleAdapter.query(s)
    }

    fun openHistory() {
        (context as? FragmentActivity)?.let {
            ViewModelProvider(it).get(ContentViewModel::class.java)
                .nextFragment(SearchHistoryFragment::class.java)
        }
    }

    /**
     * Clear search history.
     */
    fun confirmClear() {
        val activityContext = context
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
        if (!isVisible && enable) {
            runOnMainThread { isVisible = true }
        }
    }

    /**
     * Hide this module.
     */
    fun hide() {
        if (isVisible) {
            runOnMainThread { isVisible = false }
        }
    }

    fun setViewModel(viewModel: SearchFragmentViewModel) {
        moduleAdapter.setViewModel(viewModel)
    }

    private fun runOnMainThread(action: () -> Unit) =
            CoroutineScope(Dispatchers.Main).launch { action() }

    /**
     * Dispose last subscription.
     */
    fun dispose() {
        disposable?.cancel()
    }

}
