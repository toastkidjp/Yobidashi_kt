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
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.RichText
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.RichTextThemeIntegration
import com.halilibo.richtext.ui.string.RichTextStringStyle
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.article.data.AppDatabase
import jp.toastkid.article_viewer.article.detail.LinkBehaviorService
import jp.toastkid.article_viewer.article.detail.LinkGenerator
import jp.toastkid.article_viewer.article.detail.viewmodel.ContentViewerFragmentViewModel
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.TabListViewModel
import jp.toastkid.lib.color.LinkColorGenerator
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.scroll.usecase.ScrollerUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ArticleContentUi(title: String) {
    val context = LocalContext.current as? ComponentActivity ?: return
    val preferenceApplier = PreferenceApplier(context)
    val viewModelProvider = ViewModelProvider(context)
    val repository = remember { AppDatabase.find(context).articleRepository() }
    val linkBehaviorService = remember {
        LinkBehaviorService(
            viewModelProvider.get(ContentViewModel::class.java),
            viewModelProvider.get(BrowserViewModel::class.java),
            { repository.exists(it) > 0 }
        )
    }
    val viewModel = viewModel(ContentViewerFragmentViewModel::class.java)

    viewModel.setTitle(title)

    val linkGenerator = remember { LinkGenerator() }

    LaunchedEffect(key1 = title, block = {
        val content = withContext(Dispatchers.IO) {
            repository.findContentByTitle(title)
        }

        if (content.isNullOrBlank()) {
            return@LaunchedEffect
        }

        val converted = linkGenerator.invoke(content)
        viewModel.setContent(converted)
    })

    val contentViewModel = viewModel(ContentViewModel::class.java, context)
    contentViewModel.replaceAppBarContent {
        AppBarContent(viewModel)
    }

    val scrollState = rememberScrollState()

        /*
binding.content.highlightColor = preferenceApplier.editorHighlightColor(Color.CYAN)*/

    val editorFontColor = preferenceApplier.editorFontColor()
    val stringStyle = RichTextStringStyle(
        linkStyle = SpanStyle(Color(LinkColorGenerator().invoke(editorFontColor)))
    )

    SelectionContainer {
        RichTextThemeIntegration(
            contentColor = { Color(editorFontColor) }
        ) {
            RichText(
                style = RichTextStyle(
                    headingStyle = { level, textStyle ->
                        when (level) {
                            0 -> TextStyle(
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                            1 -> TextStyle(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                            2 -> TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            3 -> TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            4 -> TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textStyle.color.copy(alpha = .7F)
                            )
                            5 -> TextStyle(
                                fontWeight = FontWeight.Bold,
                                color = textStyle.color.copy(alpha = .7f)
                            )
                            else -> textStyle
                        }
                    },
                    stringStyle = stringStyle
                ),
                modifier = Modifier
                    .background(Color(preferenceApplier.editorBackgroundColor()))
                    .padding(8.dp)
                    .verticalScroll(scrollState)
            ) {
                Markdown(
                    viewModel.content.value,
                    onLinkClicked = {
                        linkBehaviorService.invoke(it)
                    }
                )
            }
        }
    }

    ScrollerUseCase(contentViewModel, scrollState).invoke(LocalLifecycleOwner.current)

    contentViewModel.clearOptionMenus()
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun AppBarContent(viewModel: ContentViewerFragmentViewModel) {
    val activityContext = LocalContext.current as? ComponentActivity ?: return
    val preferenceApplier = PreferenceApplier(activityContext)
    val tabListViewModel = viewModel(TabListViewModel::class.java, activityContext)

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
                        viewModel.title.value,
                        color = Color(preferenceApplier.editorFontColor())
                    )
                },
                singleLine = true,
                keyboardActions = KeyboardActions {
                    //TODO contentTextSearchUseCase.invoke(it.toString())
                },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colorScheme.onPrimary,
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
                    onClick = {
                        ViewModelProvider(activityContext)
                            .get(ContentViewModel::class.java)
                            .switchTabList()
                    },
                    onLongClick = {
                        tabListViewModel.openNewTabForLongTap()
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
                text = tabListViewModel.tabCount.value.toString(),
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(start = 2.dp, bottom = 2.dp)
            )
        }
    }
}
