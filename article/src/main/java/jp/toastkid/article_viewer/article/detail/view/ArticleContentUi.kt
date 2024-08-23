/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.detail.view

import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.article.data.ArticleRepositoryFactory
import jp.toastkid.article_viewer.article.detail.viewmodel.ContentViewerFragmentViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.scroll.usecase.ScrollerUseCase
import jp.toastkid.markdown.domain.service.LinkGenerator
import jp.toastkid.markdown.presentation.MarkdownPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ArticleContentUi(title: String, modifier: Modifier) {
    val context = LocalContext.current as? ComponentActivity ?: return
    val viewModel = remember { ContentViewerFragmentViewModel() }

    LaunchedEffect(key1 = title, block = {
        val content = withContext(Dispatchers.IO) {
            ArticleRepositoryFactory().invoke(context).findContentByTitle(title)
        }

        if (content.isNullOrBlank()) {
            return@LaunchedEffect
        }

        viewModel.setTitle(title)

        val converted = LinkGenerator().invoke(content)
        viewModel.setContent(converted)
    })

    val contentViewModel = viewModel(ContentViewModel::class.java, context)
    contentViewModel.replaceAppBarContent {
        AppBarContent(viewModel)
    }

    Surface(
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 4.dp,
        modifier = modifier
    ) {
        MarkdownPreview(
            content = viewModel.content(),
            scrollState = viewModel.scrollState(),
            modifier = Modifier.padding(8.dp)
        )
    }

    ScrollerUseCase(contentViewModel, viewModel.scrollState()).invoke(LocalLifecycleOwner.current)

    contentViewModel.clearOptionMenus()
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun AppBarContent(viewModel: ContentViewerFragmentViewModel) {
    val activityContext = LocalContext.current as? ComponentActivity ?: return
    val preferenceApplier = PreferenceApplier(activityContext)
    val contentViewModel = viewModel(ContentViewModel::class.java, activityContext)

    var searchInput by remember { mutableStateOf("") }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
    ) {
        Column(Modifier.weight(1f)) {
            TextField(
                value = searchInput,
                onValueChange = {
                    searchInput = it
                },
                label = {
                    Text(
                        viewModel.title(),
                        color = Color(preferenceApplier.editorFontColor())
                    )
                },
                singleLine = true,
                keyboardActions = KeyboardActions {
                    //TODO contentTextSearchUseCase.invoke(it.toString())
                },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary.copy(0.75f),
                    cursorColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    focusedIndicatorColor = MaterialTheme.colorScheme.onPrimary
                ),
                trailingIcon = {
                    Icon(
                        Icons.Filled.Clear,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        contentDescription = "clear text",
                        modifier = Modifier
                            .offset(x = 8.dp)
                            .clickable {
                                searchInput = ""
                            }
                    )
                }
            )
        }
        Box(
            Modifier
                .width(40.dp)
                .fillMaxHeight()
                .combinedClickable(
                    true,
                    onClick = contentViewModel::switchTabList,
                    onLongClick = {
                        contentViewModel.openNewTab()
                    }
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_tab),
                contentDescription = stringResource(id = R.string.tab),
                colorFilter = ColorFilter.tint(
                    MaterialTheme.colorScheme.onPrimary,
                    BlendMode.SrcIn
                ),
                modifier = Modifier.align(Alignment.Center)
            )
            Text(
                text = contentViewModel.tabCount.value.toString(),
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(start = 2.dp, bottom = 2.dp)
            )
        }
    }
}
