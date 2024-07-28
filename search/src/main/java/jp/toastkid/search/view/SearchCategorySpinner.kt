/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.search.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.SearchCategory

@Composable
internal fun SearchCategorySpinner(
    expand: Boolean,
    openSpinner: () -> Unit,
    closeSpinner: () -> Unit,
    currentCategory: String,
    onSelect: (SearchCategory) -> Unit = {}
) {
    val initialDisables = PreferenceApplier(LocalContext.current).readDisableSearchCategory()
    val popupWindowHeight = (LocalConfiguration.current.screenHeightDp / 2).dp

    Box(
        modifier = Modifier
            .clickable(onClick = openSpinner)
            .width(44.dp)
            .height(56.dp)
            .drawBehind { drawRect(Color(0xDDFFFFFF)) }
    ) {
        val category = SearchCategory.findByCategory(currentCategory)

        AsyncImage(
            category.iconId,
            contentDescription = stringResource(id = category.id),
            contentScale = ContentScale.Fit,
            alignment = Alignment.Center,
            modifier = Modifier
                .fillMaxHeight()
                .padding(4.dp)
        )

        DropdownMenu(
            expanded = expand,
            onDismissRequest = closeSpinner
        ) {
            val searchCategories = SearchCategory.values()
                .filterNot { initialDisables?.contains(it.name) ?: false }

            LazyColumn(modifier = Modifier.size(popupWindowHeight)) {
                items(searchCategories, { it.id }) { searchCategory ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = searchCategory.iconId,
                                    contentDescription = stringResource(id = searchCategory.id),
                                    modifier = Modifier.width(40.dp)
                                )
                                Text(
                                    stringResource(id = searchCategory.id),
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        },
                        onClick = {
                            onSelect(searchCategory)
                            closeSpinner()
                        }
                    )
                }
            }
        }
    }
}