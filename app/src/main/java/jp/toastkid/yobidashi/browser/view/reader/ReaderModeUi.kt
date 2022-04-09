/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.view.reader

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R

@Composable
internal fun ReaderModeUi(title: String, text: MutableState<String>) {
    val preferenceApplier = PreferenceApplier(LocalContext.current)

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color(preferenceApplier.editorBackgroundColor()))
            .padding(16.dp)
    ) {
        Column(Modifier.verticalScroll(scrollState)) {
            SelectionContainer {
                Text(
                    text = title,
                    color = Color(preferenceApplier.editorFontColor()),
                    fontSize = 30.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            SelectionContainer {
                Text(
                    text = text.value,
                    color = Color(preferenceApplier.editorFontColor()),
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                )
            }
        }
        Image(
            painterResource(R.drawable.ic_close_black),
            contentDescription = stringResource(id = R.string.close),
            colorFilter = ColorFilter.tint(Color(preferenceApplier.fontColor), BlendMode.SrcIn),
            modifier = Modifier
                .size(60.dp)
                .padding(16.dp)
                .align(Alignment.TopEnd)
                .clickable {
                    text.value = ""
                }
        )
    }

}