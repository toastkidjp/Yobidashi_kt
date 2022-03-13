/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.rss

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.fragment.CommonFragmentAction
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.rss.api.RssReaderApi
import jp.toastkid.rss.model.Item
import jp.toastkid.rss.setting.RssSettingFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class RssReaderFragment : Fragment(), CommonFragmentAction, ContentScrollable {

    private var scrollState: LazyListState? = null

    private val disposables = Job()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        val context = context ?: return super.onCreateView(inflater, container, savedInstanceState)

        val composeView = ComposeView(context)
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

        CoroutineScope(Dispatchers.IO).launch(disposables) {
            val readRssReaderTargets = PreferenceApplier(context).readRssReaderTargets()
            readRssReaderTargets.asFlow()
                .mapNotNull { RssReaderApi().invoke(it) }
                .collect {
                    withContext(Dispatchers.Main) {
                        val items = it.items
                        composeView.setContent {
                            RssReaderListUi(items)
                        }
                    }
                }
        }

        return composeView
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun RssReaderListUi(items: MutableList<Item>) {
        val fragmentActivity = activity ?: return
        val browserViewModel = ViewModelProvider(fragmentActivity)
            .get(BrowserViewModel::class.java)

        val listState = rememberLazyListState()
        this.scrollState = listState

        MaterialTheme {
            LazyColumn(state = listState) {
                items(items) {
                    Surface(
                        modifier = Modifier
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 2.dp,
                                bottom = 2.dp
                            )
                            .combinedClickable(
                                enabled = true,
                                onClick = {
                                    browserViewModel.open(it.link.toUri())
                                    activity?.supportFragmentManager?.popBackStack()
                                },
                                onLongClick = {
                                    browserViewModel.openBackground(it.link.toUri())
                                }
                            ),
                        elevation = 4.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                        ) {
                            AsyncImage(
                                R.drawable.ic_rss_feed,
                                contentDescription = stringResource(id = R.string.image),
                                modifier = Modifier.width(32.dp),
                                colorFilter = ColorFilter.tint(
                                    colorResource(id = R.color.colorPrimary),
                                    BlendMode.SrcIn
                                )
                            )
                            Column {
                                Text(
                                    text = it.title,
                                    fontSize = 18.sp,
                                    maxLines = 1,
                                )
                                Text(
                                    text = it.link,
                                    color = colorResource(R.color.link_blue),
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = it.content.toString(),
                                    fontSize = 14.sp,
                                    maxLines = 3,
                                )
                                Text(
                                    text = it.source,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                )
                                Text(
                                    text = it.date,
                                    color = colorResource(R.color.darkgray_scale),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun toTop() {
        CoroutineScope(Dispatchers.Main).launch {
            scrollState?.scrollToItem(0, 0)
        }
    }

    override fun toBottom() {
        CoroutineScope(Dispatchers.Main).launch {
            scrollState?.scrollToItem(scrollState?.layoutInfo?.totalItemsCount ?: 0, 0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.rss_reader, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_rss_setting) {
            val activity = activity ?: return true
            ViewModelProvider(activity)
                .get(ContentViewModel::class.java)
                .nextFragment(RssSettingFragment::class.java)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun pressBack(): Boolean {
        activity?.supportFragmentManager?.popBackStack()
        return true
    }

    override fun onDetach() {
        super.onDetach()
        disposables.cancel()
    }

}