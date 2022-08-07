/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main.ui.finder

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.viewmodel.PageSearcherViewModel
import jp.toastkid.yobidashi.R

@Composable
internal fun FindInPage(
    openFindInPageState: MutableState<Boolean>,
    tint: Color,
    pageSearcherInput: MutableState<String>
) {
    val activity = LocalContext.current as? ViewModelStoreOwner ?: return
    val pageSearcherViewModel = viewModel(PageSearcherViewModel::class.java, activity)
    val closeAction = {
        pageSearcherViewModel.clearInput()
        pageSearcherViewModel.hide()
        openFindInPageState.value = false
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painterResource(id = R.drawable.ic_close),
            contentDescription = stringResource(id = R.string.content_description_close_find_area),
            tint = tint,
            modifier = Modifier
                .clickable(onClick = closeAction)
                .padding(start = 16.dp)
        )

        val focusRequester = remember { FocusRequester() }

        TextField(
            value = pageSearcherInput.value,
            onValueChange = { text ->
                pageSearcherInput.value = text
                pageSearcherViewModel.find(text)
            },
            label = {
                Text(
                    stringResource(id = R.string.hint_find_in_page),
                    color = MaterialTheme.colors.onPrimary
                )
            },
            singleLine = true,
            textStyle = TextStyle(
                color = MaterialTheme.colors.onPrimary,
                textAlign = TextAlign.Start,
            ),
            trailingIcon = {
                Icon(
                    painterResource(R.drawable.ic_clear_form),
                    contentDescription = "clear text",
                    tint = MaterialTheme.colors.onPrimary,
                    modifier = Modifier
                        .clickable {
                            pageSearcherInput.value = ""
                            pageSearcherViewModel.clearInput()
                        }
                )
            },
            maxLines = 1,
            keyboardActions = KeyboardActions {
                pageSearcherViewModel.findDown(pageSearcherInput.value)
            },
            keyboardOptions = KeyboardOptions(
                autoCorrect = true,
                imeAction = ImeAction.Search
            ),
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
                .background(Color.Transparent)
                .focusRequester(focusRequester)
        )
        Icon(
            painterResource(id = R.drawable.ic_up),
            contentDescription = stringResource(id = R.string.content_description_find_upward),
            tint = tint,
            modifier = Modifier
                .clickable {
                    pageSearcherViewModel.findUp(
                        pageSearcherInput.value
                    )
                }
                .padding(8.dp)
        )
        Icon(
            painterResource(id = R.drawable.ic_down),
            contentDescription = stringResource(id = R.string.content_description_find_downward),
            tint = tint,
            modifier = Modifier
                .clickable {
                    pageSearcherViewModel.findDown(pageSearcherInput.value)
                }
                .padding(8.dp)
        )

        BackHandler(openFindInPageState.value) {
            closeAction()
        }

        LaunchedEffect(key1 = "find_in_page_first_launch", block = {
            if (openFindInPageState.value) {
                focusRequester.requestFocus()
            }
        })
    }
}