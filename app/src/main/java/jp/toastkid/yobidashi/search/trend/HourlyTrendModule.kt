/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.trend

import androidx.core.view.isVisible
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.databinding.ModuleSearchHourlyTrendBinding
import jp.toastkid.yobidashi.search.SearchFragmentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException

/**
 * @author toastkidjp
 */
class HourlyTrendModule(
        private val binding: ModuleSearchHourlyTrendBinding?,
        viewModel: SearchFragmentViewModel
) {

    private val trendApi = TrendApi()

    private var adapter: Adapter? = null

    private var lastJob: Job = Job()

    private var enable: Boolean

    init {
        val context = binding?.root?.context
        enable = if (context == null) false else PreferenceApplier(context).isEnableTrendModule()

        adapter = Adapter(viewModel)
        binding?.trendItems?.adapter = adapter
        val layoutManager = makeLayoutManager()
        binding?.trendItems?.layoutManager = layoutManager
    }

    private fun makeLayoutManager(): FlexboxLayoutManager {
        val layoutManager = FlexboxLayoutManager(binding?.root?.context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP
        layoutManager.justifyContent = JustifyContent.FLEX_START
        layoutManager.alignItems = AlignItems.STRETCH
        return layoutManager
    }

    fun request() {
        lastJob.cancel()
        if (!enable) {
            return
        }

        lastJob = CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                val trendItems = try {
                    trendApi()
                } catch (e: IOException) {
                    Timber.e(e)
                    null
                }
                adapter?.replace(trendItems?.take(10))
            }
            binding?.root?.isVisible = adapter?.isNotEmpty() ?: false
            adapter?.notifyDataSetChanged()
        }
    }

    fun setEnable(newState: Boolean) {
        this.enable = newState

        binding?.root?.isVisible = newState
    }

    fun dispose() {
        lastJob.cancel()
    }
}