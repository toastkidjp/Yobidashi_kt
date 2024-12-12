/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main.ui.finder

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
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
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.yobidashi.R

@Composable
internal fun FindInPage() {
    val activity = LocalContext.current as? ViewModelStoreOwner ?: return
    val contentViewModel = viewModel(ContentViewModel::class.java, activity)
    val pageSearcherInput = remember { mutableStateOf("") }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painterResource(id = R.drawable.ic_close),
            contentDescription = stringResource(id = jp.toastkid.article_viewer.R.string.content_description_close_find_area),
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .clickable(onClick = contentViewModel::closeFindInPage)
                .padding(start = 16.dp)
        )

        val focusRequester = remember { FocusRequester() }

        TextField(
            value = pageSearcherInput.value,
            onValueChange = { text ->
                pageSearcherInput.value = text
                contentViewModel.find(text)
            },
            label = {
                Text(
                    stringResource(id = R.string.hint_find_in_page),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            },
            singleLine = true,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Start,
            ),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.onPrimary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            trailingIcon = {
                Icon(
                    painterResource(jp.toastkid.lib.R.drawable.ic_clear_form),
                    contentDescription = "clear text",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .clickable {
                            pageSearcherInput.value = ""
                            contentViewModel.clearFinderInput()
                        }
                )
            },
            maxLines = 1,
            keyboardActions = KeyboardActions {
                contentViewModel.findDown(pageSearcherInput.value)
            },
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = true,
                imeAction = ImeAction.Search
            ),
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
                .drawBehind { drawRect(Color.Transparent) }
                .focusRequester(focusRequester)
        )
        Icon(
            painterResource(id = R.drawable.ic_up),
            contentDescription = stringResource(id = jp.toastkid.article_viewer.R.string.content_description_find_upward),
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .clickable {
                    contentViewModel.findUp(
                        pageSearcherInput.value
                    )
                }
                .padding(8.dp)
        )
        Icon(
            painterResource(id = R.drawable.ic_down),
            contentDescription = stringResource(id = jp.toastkid.article_viewer.R.string.content_description_find_downward),
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .clickable {
                    contentViewModel.findDown(pageSearcherInput.value)
                }
                .padding(8.dp)
        )

        BackHandler(contentViewModel.openFindInPageState.value) {
            contentViewModel.closeFindInPage()
        }

        LaunchedEffect(key1 = "find_in_page_first_launch", block = {
            if (contentViewModel.openFindInPageState.value) {
                focusRequester.requestFocus()
            }
        })
    }
}