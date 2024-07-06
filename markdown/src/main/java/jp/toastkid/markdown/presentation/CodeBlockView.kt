/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.markdown.presentation


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import jp.toastkid.markdown.domain.model.data.CodeBlockLine

@Composable
internal fun CodeBlockView(line: CodeBlockLine, fontSize: TextUnit = 28.sp, modifier: Modifier = Modifier) {
    val viewModel = remember { CodeBlockViewModel() }

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
        shadowElevation = 4.dp
    ) {
        BasicTextField(
            value = viewModel.content(),
            onValueChange = viewModel::onValueChange,
            onTextLayout = {
                viewModel.setMultiParagraph(it.multiParagraph)
            },
            visualTransformation = {
                viewModel.transform(it)
            },
            decorationBox = {
                Row {
                    Column(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .wrapContentSize(unbounded = true)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                    ) {
                        viewModel.lineNumberTexts().forEach {
                            Box(
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    it,
                                    fontSize = fontSize,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.End,
                                    lineHeight = 1.5.em
                                )
                            }
                        }
                    }
                    it()
                }
            },
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = fontSize,
                fontFamily = FontFamily.Monospace,
                lineHeight = 1.55.em
            ),
            modifier = modifier
                .background(MaterialTheme.colorScheme.surface)
                .height(viewModel.maxHeight(fontSize))
        )

    }

    LaunchedEffect(line.code) {
        viewModel.start(line.code)
    }
}