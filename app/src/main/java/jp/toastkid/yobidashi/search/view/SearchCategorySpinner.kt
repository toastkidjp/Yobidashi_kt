/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.SearchCategory
import jp.toastkid.yobidashi.R

@Composable
internal fun SearchCategorySpinner(
    spinnerOpen: MutableState<Boolean>,
    currentCategory: MutableState<String>?,
    onSelect: (SearchCategory) -> Unit = {}
) {
    val initialDisables = PreferenceApplier(LocalContext.current).readDisableSearchCategory()

    Box(
        modifier = Modifier
            .clickable {
                spinnerOpen.value = true
            }
            .width(dimensionResource(id = R.dimen.search_category_spinner_width))
            .fillMaxHeight()
            .background(colorResource(id = R.color.spinner_background))
    ) {
        val category = SearchCategory.findByCategory(currentCategory?.value)

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
            expanded = spinnerOpen.value,
            onDismissRequest = { spinnerOpen.value = false }
        ) {
            val searchCategories = SearchCategory.values()
                .filterNot { initialDisables?.contains(it.name) ?: false }
            searchCategories.forEach { searchCategory ->
                DropdownMenuItem(
                    onClick = {
                        currentCategory?.value = searchCategory.name
                        onSelect(searchCategory)
                        spinnerOpen.value = false
                    }
                ) {
                    AsyncImage(
                        model = searchCategory.iconId,
                        contentDescription = stringResource(id = searchCategory.id),
                        modifier = Modifier.width(40.dp)
                    )
                    Text(
                        stringResource(id = searchCategory.id),
                        color = colorResource(id = R.color.black),
                        fontSize = 20.sp,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}