/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.pdf

import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.fragment.CommonFragmentAction
import jp.toastkid.lib.scroll.rememberViewInteropNestedScrollConnection
import jp.toastkid.lib.tab.OnBackCloseableTabUiFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * @author toastkidjp
 */
class PdfViewerFragment : Fragment(), OnBackCloseableTabUiFragment, CommonFragmentAction,
    ContentScrollable {

    private var scrollState: LazyListState? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = activity ?: return null
        val composeView = ComposeView(context)
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        arguments?.let { arguments ->
            arguments.getParcelable<Uri>(KEY_URI)?.also { uri ->
                val pdfRenderer =
                    context.contentResolver.openFileDescriptor(uri, "r")
                        ?.let { PdfRenderer(it) }
                        ?: return null
                composeView.setContent {
                    PdfViewerUi(pdfRenderer)
                }
            }
        }

        ViewModelProvider(context).get(AppBarViewModel::class.java).replace(context) { AppBarUi() }

        return composeView
    }

    @Composable
    fun PdfViewerUi(pdfRenderer: PdfRenderer) {
        val pdfImageFactory = PdfImageFactory()

        val listState = rememberLazyListState()
        this.scrollState = listState

        MaterialTheme {
            LazyColumn(
                state = listState,
                modifier = Modifier.nestedScroll(rememberViewInteropNestedScrollConnection())
            ) {
                val max = pdfRenderer.pageCount
                items(max) {
                    androidx.compose.material.Surface(
                        modifier = Modifier
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 2.dp,
                                bottom = 2.dp
                            )
                    ) {
                        Column {
                            AsyncImage(
                                model = pdfImageFactory.invoke(pdfRenderer.openPage(it)),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "${it + 1} / $max",
                                color = colorResource(id = R.color.black),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun AppBarUi() {
        var sliderPosition by remember { mutableStateOf(0f) }
        Slider(
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
                CoroutineScope(Dispatchers.Main).launch {
                    scrollState?.scrollToItem(
                        ((scrollState?.layoutInfo?.totalItemsCount ?: 0 ) * it).roundToInt(),
                        0
                    )
                }
            },
            steps = (scrollState?.layoutInfo?.totalItemsCount ?: 2) - 1
        )
    }

    /**
     * Animate root view.
     *
     * @param animation
     */
    fun animate(animation: Animation) {
        view?.startAnimation(animation)
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

    fun setInitialArguments(uri: Uri?, scrolled: Int) {
        arguments = bundleOf(KEY_URI to uri)
    }

    companion object {

        private const val KEY_URI = "uri"

    }
}