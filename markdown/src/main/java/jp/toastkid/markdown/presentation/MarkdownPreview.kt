/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.markdown.presentation

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
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.toastkid.markdown.domain.model.data.CodeBlockLine
import jp.toastkid.markdown.domain.model.data.HorizontalRule
import jp.toastkid.markdown.domain.model.data.ImageLine
import jp.toastkid.markdown.domain.model.data.ListLine
import jp.toastkid.markdown.domain.model.data.TableLine
import jp.toastkid.markdown.domain.model.data.TextBlock
import jp.toastkid.markdown.domain.model.entity.Markdown
import jp.toastkid.ui.image.EfficientImage
import jp.toastkid.ui.menu.context.common.CommonContextMenuToolbarFactory

@Composable
fun MarkdownPreview(
    content: Markdown,
    contentColor: Color,
    scrollState: ScrollState,
    modifier: Modifier
) {
    val viewModel = remember { MarkdownPreviewViewModel(scrollState) }

    CompositionLocalProvider(
        LocalTextToolbar provides CommonContextMenuToolbarFactory().invoke(LocalView.current)
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
                                        color = if (line.quote) Color(0xFFCCAAFF) else contentColor,
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
                                        TextStyle(color = contentColor, fontSize = 14.sp),
                                        Modifier.padding(bottom = 4.dp)
                                    )
                                }
                            }
                        }

                        is ImageLine -> {
                            EfficientImage(
                                model = line.source,
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