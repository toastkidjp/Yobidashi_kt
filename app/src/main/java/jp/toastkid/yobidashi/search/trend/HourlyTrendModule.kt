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
import jp.toastkid.yobidashi.databinding.ModuleSearchHourlyTrendBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class HourlyTrendModule(
        private val hourlyTrendModule: ModuleSearchHourlyTrendBinding?,
        browseCallback: (String) -> Unit,
        browseBackgroundCallback: (String) -> Unit
) {

    private val trendApi = TrendApi()

    private var adapter: Adapter? = null

    private var lastJob: Job = Job()

    init {
        adapter = Adapter(browseCallback, browseBackgroundCallback)
        hourlyTrendModule?.trendItems?.adapter = adapter
        val layoutManager = FlexboxLayoutManager(hourlyTrendModule?.root?.context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP
        layoutManager.justifyContent = JustifyContent.FLEX_START
        layoutManager.alignItems = AlignItems.STRETCH
        hourlyTrendModule?.trendItems?.layoutManager = layoutManager
    }

    fun request() {
        lastJob.cancel()
        lastJob = CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                adapter?.replace(trendApi())
            }
            hourlyTrendModule?.root?.isVisible = adapter?.isNotEmpty() ?: false
            adapter?.notifyDataSetChanged()
        }
    }

    fun dispose() {
        lastJob.cancel()
    }
}