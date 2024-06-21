/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.markdown.presentation

import android.view.Menu
import android.view.MenuInflater
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.clip.Clipboard
import jp.toastkid.markdonw.R
import jp.toastkid.markdown.domain.model.data.CodeBlockLine
import jp.toastkid.markdown.domain.model.data.HorizontalRule
import jp.toastkid.markdown.domain.model.data.ImageLine
import jp.toastkid.markdown.domain.model.data.ListLine
import jp.toastkid.markdown.domain.model.data.TableLine
import jp.toastkid.markdown.domain.model.data.TextBlock
import jp.toastkid.markdown.domain.model.entity.Markdown
import jp.toastkid.ui.menu.context.ContextMenuToolbar
import jp.toastkid.ui.menu.context.MenuActionCallback
import jp.toastkid.ui.menu.context.MenuInjector

@Composable
fun MarkdownPreview(
    content: Markdown,
    scrollState: ScrollState,
    modifier: Modifier
) {
    val viewModel = remember { MarkdownPreviewViewModel(scrollState) }
    val context = LocalContext.current
    val viewModelStoreOwner = context as ViewModelStoreOwner
    val contentViewModel = ViewModelProvider(viewModelStoreOwner).get(ContentViewModel::class.java)

    CompositionLocalProvider(
        LocalTextToolbar provides ContextMenuToolbar(
            LocalView.current,
            object : MenuInjector {
                override fun invoke(menu: Menu?) {
                    val menuInflater = MenuInflater(context)

                    menuInflater.inflate(R.menu.context_article_content_search, menu)
                }
            },
            object : MenuActionCallback {

                override fun invoke(
                    menuId: Int,
                    onCopyRequested: (() -> Unit)?,
                    onPasteRequested: (() -> Unit)?,
                    onCutRequested: (() -> Unit)?,
                    onSelectAllRequested: (() -> Unit)?
                ): Boolean = when (menuId) {
                    R.id.copy -> {
                        onCopyRequested?.invoke()
                        true
                    }
                    R.id.select_all -> {
                        onSelectAllRequested?.invoke()
                        true
                    }
                    R.id.preview_search -> {
                        val present = Clipboard.getPrimary(context)
                        onCopyRequested?.invoke()
                        val primary = Clipboard.getPrimary(context)
                        Clipboard.clip(context, present?.toString() ?: "")
                        if (primary != null && primary.isNotBlank()) {
                            contentViewModel.preview(primary.toString())
                        }
                        true
                    }
                    R.id.web_search -> {
                        /*val selection = currentSelection()
                        if (selection.isEmpty()) {
                            return false
                        }*/
                        val present = Clipboard.getPrimary(context)
                        onCopyRequested?.invoke()
                        val primary = Clipboard.getPrimary(context)
                        Clipboard.clip(context, present?.toString() ?: "")
                        if (primary != null && primary.isNotBlank()) {
                            contentViewModel.search(primary.toString())
                        }
                        true
                    }
                    else -> false
                }
            }
        )
    ) {
        SelectionContainer {
            Column(modifier = modifier
                .verticalScroll(scrollState)
                .padding(8.dp)) {
                content.lines().forEach { line ->
                    when (line) {
                        is TextBlock -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (line.quote) {
                                    VerticalDivider(
                                        modifier = Modifier
                                            .padding(start = 4.dp, end = 8.dp)
                                            .height(36.dp),
                                        thickness = 2.dp,
                                        color = Color(0x88CCAAFF),
                                    )
                                }
                                TextLineView(
                                    line.text,
                                    TextStyle(
                                        color = if (line.quote) Color(0xFFCCAAFF) else MaterialTheme.colorScheme.onSurface,
                                        fontSize = line.fontSize().sp,
                                        fontWeight = viewModel.makeFontWeight(line.level),
                                    ),
                                    Modifier.padding(bottom = 8.dp, top = viewModel.makeTopMargin(line.level).dp)
                                )
                            }
                        }

                        is ListLine -> Column {
                            line.list.forEachIndexed { index, it ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    when {
                                        line.ordered -> DisableSelection {
                                            Text("${index + 1}. ", fontSize = 14.sp)
                                        }

                                        line.taskList -> Checkbox(
                                            checked = it.startsWith("[x]"),
                                            enabled = false,
                                            onCheckedChange = null,
                                            modifier = Modifier.size(32.dp)
                                        )

                                        else -> DisableSelection {
                                            Text("・ ", fontSize = 14.sp)
                                        }
                                    }
                                    TextLineView(
                                        viewModel.extractText(it, line.taskList),
                                        TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp),
                                        Modifier.padding(bottom = 4.dp)
                                    )
                                }
                            }
                        }

                        is ImageLine -> {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(line.source)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = line.source,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        is HorizontalRule -> HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        is TableLine -> TableLineView(line, 16.sp, Modifier.padding(bottom = 8.dp))
                        is CodeBlockLine -> CodeBlockView(line, 16.sp, Modifier.padding(bottom = 8.dp))
                    }
                }
            }
        }
    }
}