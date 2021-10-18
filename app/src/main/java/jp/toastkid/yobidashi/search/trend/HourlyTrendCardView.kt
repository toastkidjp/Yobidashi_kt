/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.trend

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ViewCardHourlyTrendBinding
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
class HourlyTrendCardView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private var binding: ViewCardHourlyTrendBinding? = null

    private val trendApi = TrendApi()

    private var adapter: Adapter? = null

    private var lastJob: Job = Job()

    init {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.view_card_hourly_trend,
            this,
            true
        )

        adapter = Adapter()
        binding?.trendItems?.adapter = adapter
        binding?.trendItems?.layoutManager = makeLayoutManager()

        binding?.module = this
    }

    private fun makeLayoutManager(): RecyclerView.LayoutManager {
        val layoutManager = FlexboxLayoutManager(binding?.root?.context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP
        layoutManager.justifyContent = JustifyContent.FLEX_START
        layoutManager.alignItems = AlignItems.STRETCH
        return layoutManager
    }

    fun hide() {
        isVisible = false
    }

    fun request() {
        lastJob.cancel()
        if (!isEnabled) {
            isVisible = false
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
            isVisible = adapter?.isNotEmpty() ?: false
            adapter?.notifyDataSetChanged()
        }
    }

    fun setViewModel(viewModel: SearchFragmentViewModel) {
        adapter?.setViewModel(viewModel)
    }

    fun openMore() {
        (binding?.root?.context as? FragmentActivity)?.let {
            ViewModelProvider(it).get(BrowserViewModel::class.java)
                .open(fullContentUri)
        }
    }

    fun dispose() {
        lastJob.cancel()
        binding = null
    }

    companion object {

        private val fullContentUri =
            "https://trends.google.co.jp/trends/trendingsearches/realtime".toUri()

    }
}