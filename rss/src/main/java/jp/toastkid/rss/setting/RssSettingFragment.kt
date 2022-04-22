/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.rss.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.fragment.CommonFragmentAction
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.rss.R

/**
 * @author toastkidjp
 */
class RssSettingFragment : Fragment(), CommonFragmentAction {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val context = context ?: return super.onCreateView(inflater, container, savedInstanceState)
        val composeView = ComposeView(context)
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

        val fragmentActivity = activity
            ?: return super.onCreateView(inflater, container, savedInstanceState)
        val preferenceApplier = PreferenceApplier(fragmentActivity)
        val rssReaderTargets = preferenceApplier.readRssReaderTargets()
        if (rssReaderTargets.isEmpty()) {
            ViewModelProvider(fragmentActivity).get(ContentViewModel::class.java)
                .snackShort(R.string.message_rss_reader_launch_failed)
            fragmentActivity.supportFragmentManager.popBackStack()
            return super.onCreateView(inflater, container, savedInstanceState)
        }

        composeView.setContent { RssSettingUi(rssReaderTargets.toMutableList()) }
        return composeView
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun RssSettingUi(rssReaderTargets: MutableList<String>) {
        MaterialTheme {
            LazyColumn {
                items(rssReaderTargets) {
                    val dismissState = rememberDismissState(
                        confirmStateChange = { dismissValue ->
                            if (dismissValue == DismissValue.DismissedToStart){
                                deleteItem(rssReaderTargets, it)
                            }
                            true
                        }
                    )

                    Surface(
                        modifier = Modifier
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 2.dp,
                                bottom = 2.dp
                            ),
                        elevation = 4.dp
                    ) {
                        SwipeToDismiss(
                            state = dismissState,
                            modifier = Modifier.padding(vertical = 4.dp),
                            directions = setOf(
                                DismissDirection.EndToStart
                            ),
                            dismissThresholds = { direction ->
                                FractionalThreshold(if (direction == DismissDirection.StartToEnd) 0.25f else 0.5f)
                            },
                            background = {
                                val direction =
                                    dismissState.dismissDirection ?: return@SwipeToDismiss
                                val color by animateColorAsState(
                                    when (dismissState.targetValue) {
                                        DismissValue.DismissedToStart -> Color(0xFFDD4444)
                                        else -> Color(0x88DD4444)
                                    }
                                )
                                val alignment = when (direction) {
                                    DismissDirection.StartToEnd -> Alignment.CenterStart
                                    DismissDirection.EndToStart -> Alignment.CenterEnd
                                }
                                val icon = when (direction) {
                                    DismissDirection.StartToEnd -> null
                                    DismissDirection.EndToStart -> Icons.Default.Delete
                                }
                                val scale by animateFloatAsState(
                                    if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
                                )

                                if (icon != null) {
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .background(color)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = alignment
                                    ) {
                                        Icon(
                                            icon,
                                            contentDescription = "Localized description",
                                            modifier = Modifier.scale(scale)
                                        )
                                    }
                                }
                            },
                            dismissContent = {
                                Text(
                                    text = it,
                                    fontSize = 18.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight()
                                        .padding(
                                            start = 16.dp,
                                            end = 16.dp,
                                            top = 2.dp,
                                            bottom = 2.dp
                                        )
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    private fun deleteItem(
        rssReaderTargets: MutableList<String>,
        it: String
    ) {
        rssReaderTargets.remove(it)
        context?.let { context ->
            PreferenceApplier(context).removeFromRssReaderTargets(it)
        }
    }

    override fun pressBack(): Boolean {
        activity?.supportFragmentManager?.popBackStack()
        return true
    }

}